package tv.yap.ops

import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import com.google.common.base.{Ticker, Stopwatch}
import org.apache.http.client.methods.HttpGet
import net.liftweb.json
import json.JsonParser.ParseException
import tv.yap.model.user.{YapUserManager, YapUser}
import com.foursquare.rogue.Rogue._
import org.apache.http._
import org.apache.commons.codec.binary.Base64
import util.{EntityUtils, CharArrayBuffer, EncodingUtils}
import org.apache.http.auth.AUTH
import org.apache.http.message.BufferedHeader
import tv.yap.util.DbUtil
import org.joda.time.DateTime
import client.{HttpResponseException, ResponseHandler}
import scala.Left
import scala.Right

case class ApiTestResult(name:String,started:DateTime,durMillis:Int,status:Either[String,String])

case class ApiTest(name:String,url:String,checker:Either[String,String]=>Either[String,String],needsAuth:Boolean=false)

class YapResponseHandler extends ResponseHandler[HttpResponse] {

  /**
   * Returns the response body as a String if the response was successful (a
   * 2xx status code). If no response body exists, this returns null. If the
   * response was unsuccessful (>= 300 status code), throws an
   * [[org.apache.http.client.HttpResponseException]].
   */
  def handleResponse(response:HttpResponse ):HttpResponse = response
}

class ApiTester {
  def analyze(response:HttpResponse):Either[String,String] = {
    val statusLine = response.getStatusLine;
    val entity = EntityUtils.toString(response.getEntity())
    if (statusLine.getStatusCode >= 300) {
      Left("Error: status code %d\n%s\n%s".format(statusLine.getStatusCode(),statusLine.getReasonPhrase(),entity))
    } else {
      Right(entity)
    }
  }


  val testUSA = ApiTest("USA Shows","http://api.yap.tv/s1/network_shows/USA.json",errorOrData=>{
    checkJson(errorOrData) {parsed=>
      val count = parsed.children.length
      if( count > 10 ) Right("OK - " + count) else Left("Too few entries " + count)
    }})

  val testFeed = ApiTest("Feed","https://api.yap.tv/t/yap_shows/usa_network/feed_items.json?limit=20&timezone=America/Los_Angeles&gmt_offset=-25200&d_ctry=US&d_lang=en&screen=320x480",
    errorOrData=>{checkJson(errorOrData){parsed=>
      val count = parsed.children.length
      if( count > 10 ) Right("OK - " + count) else Left("Too few entries " + count)
    }},true)

  val xmppProxy = ApiTest("Xmpp proxy for FB","http://m.yap.tv/http-bind/",errorOrData=>checkHtmlContains(errorOrData,"JEP-0124"))


  val testMain = ApiTest("Main web","http://www.yap.tv/",errorOrData=>checkHtmlContains(errorOrData,"yap.tv"))

  val tests = Array( testUSA, testFeed, testMain, xmppProxy )

  val httpClient = new DefaultHttpClient
  val stopwatch = new Stopwatch(Ticker.systemTicker())

  lazy val testUser = {
    val testUsername = "!yap-testing!"
    (YapUser where (_.username eqs testUsername) fetch(1) headOption).
      orElse(YapUserManager.createUser(testUsername,"yap-testing@testing.yap.tv",DbUtil.createRandomString))
  }


  def buildAuthHeader(authToken: String) : Header = {
    val tmp: StringBuilder = new StringBuilder
    tmp.append(authToken)
    tmp.append(":")
    val base64password: Array[Byte] = Base64.encodeBase64(EncodingUtils.getBytes(tmp.toString, "utf-8"))
    val buffer: CharArrayBuffer = new CharArrayBuffer(32)
    buffer.append(AUTH.WWW_AUTH_RESP)
    buffer.append(": Basic ")
    buffer.append(base64password, 0, base64password.length)
    return new BufferedHeader(buffer)
  }

  def download(url:String, host:String, authToken:Option[String]=None) : (Either[String,String],DateTime,Int) = {
    val started = new DateTime()
    stopwatch.start()
    val httpHost = new HttpHost(host)
    val status = try {
      val httpGet = new HttpGet(url)
      authToken.foreach ( a => httpGet.addHeader(buildAuthHeader(a)) )
      val responseHandler = new YapResponseHandler
      val response = httpClient.execute(httpHost, httpGet, responseHandler)
      analyze(response)
    } catch {
      case e: Exception => Left(e.toString)
    } finally {
    }
    stopwatch.stop()
    (status,started,stopwatch.elapsedMillis().toInt)
  }

  def parseRight(data:Either[String,String]):Either[String,json.JValue] =
    data.right.flatMap(body=>try Right(json.parse(body)) catch { case p:ParseException=> Left(p.getMessage) })

  def checkJson(data:Either[String,String])(checker:(json.JValue)=>Either[String,String]) = {
    parseRight(data).right.flatMap(checker(_))
  }

  def checkHtmlContains(data:Either[String,String],contains:String) =
    data.right.flatMap(body=>if(body.contains(contains)) Right(contains) else Left("Missing " + contains))

  def run(test:ApiTest,host:String) = {
    //val authOpt:Option[String] = if(test.needsAuth) testUser.map(_.authenticationToken.is) else None
    val authOpt:Option[String] = testUser.map(_.authenticationToken.is)
    val (result,started,elapsed) = download(test.url,host,authOpt)
    ApiTestResult(test.name,started,elapsed,test.checker(result))
  }

  def testCount = tests.length;

  def runTest(index:Int,host:String) = {
    run(tests(index),host)
  }

  def shutdown = httpClient.getConnectionManager.shutdown()

}

package code.comet

import net.liftweb.http.{PartialUpdateMsg, SHtml, CometActor}
import tv.yap.ops.{ApiTester, ApiTestResult}
import code.comet.ApiTestActor._
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.{Schedule}
import net.liftweb.util.Helpers._
import akka.dispatch.Future
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.{JsCmd, JsCmds}
import java.util.concurrent.ScheduledFuture
import xml.Text
import scala.Left
import tv.yap.ops.ApiTestResult
import scala.Some
import scala.Right
import code.comet.ApiTestActor.TestData
import xml.Text
import net.liftweb.http.js.JsCmds.SetHtml


object ApiTestActor {
  sealed trait TestRun
  object TestNoop extends TestRun
  object TestRunOne extends TestRun
  object TestStop extends TestRun
  case class TestRunServers(addresses:Array[String]) extends TestRun
  case class TestData(result:ApiTestResult) extends TestRun
}

class Remove(uid: String) extends JsCmd {
  def toJsCmd = "try{jQuery(" + ("#" + uid).encJs + ").remove();} catch (e) {}"
}

class ApiTestActor extends CometActor {


  def toggleStatusAndAction:(String,TestRun) = {
    val hasScheduled = scheduled.isDefined
    val hasInProgress = inProgress.isDefined
    if ( !hasScheduled && !hasInProgress ) {
      ("Running, click to stop",TestRunOne)
    } else if (hasScheduled ) {
      ("Stopped, click to start",TestStop)
    } else {
      ("Stopping",TestNoop)
    }
  }

  def currentState = {
    val hasScheduled = scheduled.isDefined
    val hasInProgress = inProgress.isDefined

    val state = (if ( hasScheduled )
      List("Waiting")
    else
      List()):::(if (hasInProgress) List("Testing") else List())
    partialUpdate(SetHtml("test_actor_state",Text(state.mkString(" "))))
  }

  def actorControl:JsCmd = {
    val (status,action) = toggleStatusAndAction
    this!action
    SetHtml("test_actor_current",Text(status))
  }

  def render = "#test_actor_control" #> SHtml.a(()=>actorControl,(<span id="test_actor_current">Running, click to stop</span>),"href"->"javascript:") & "#test_data *" #> <tr><td>Starting</td></tr>


  object testIterator {
    var addresses = Array("api.yap.tv")
    var testIndex = 0
    var addressIndex = 0

    def currentTest = testIndex
    def currentHost = addresses(addressIndex)
    def incrementTest {
      testIndex += 1
      if( testIndex >= apiTester.testCount ) {
        testIndex = 0
        addressIndex += 1
        if ( addressIndex >= addresses.length ) {
          addressIndex = 0
        }
      }
    }
  }

  val apiTester = new ApiTester
  var lastTests = new scala.collection.mutable.Queue[ApiTestResult]
  var scheduled:Option[ScheduledFuture[Unit]] = Some(Schedule.schedule(this,TestRunOne,5.seconds))
  var inProgress:Option[Future[ApiTestResult]] = None

  def calcId(data:ApiTestResult) = data.started.getMillisOfDay.toString

  def toHtml(data:ApiTestResult) = {
    val (status,cssClass) = data.status match {
      case Right(status) => (status,"")
      case Left(message) => ("ERR: " + message,"error")
    }
    val cid = calcId(data)
    (<tr id={cid} class={cssClass}><td>{data.name}</td> <td>
      {data.started.toLocalTime}
    </td><td>
      {data.durMillis}
    </td> <td>
      {status}
    </td></tr>)
  }


  def processMessage : PartialFunction[Any, Unit] = {
    case TestRunOne =>
      val future = Future {
        val result = apiTester.runTest(testIterator.currentTest,testIterator.currentHost)
        testIterator.incrementTest
        result
      }
      inProgress = Some(future)
      future.onComplete(f=>{
        inProgress=None
        f.foreach( this!TestData(_))
      })
    case TestStop =>
      scheduled.foreach(_.cancel(true))
      scheduled = None
    case TestData(data) =>
      inProgress = None
      scheduled = Some(Schedule.schedule(this,TestRunOne,60.seconds))
      lastTests.enqueue(data)
      partialUpdate(JqJsCmds.PrependHtml("test_data",toHtml(data)))
      if (lastTests.length>15) {
        val remove = lastTests.dequeue()
        partialUpdate(new Remove(calcId(remove)))
      }
    case TestNoop =>
      Unit
  }

  override def lowPriority = processMessage andThen {case _=> currentState}
}

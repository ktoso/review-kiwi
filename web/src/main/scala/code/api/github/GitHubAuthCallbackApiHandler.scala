package code.api.github

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.common.{Logger, Full}
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.InMemoryResponse
import com.weiglewilczek.slf4s.Logging
import org.eclipse.egit.github.core.client.GitHubClient

object GitHubAuthCallbackApiHandler extends RestHelper with Logging {

  val ErrorResponse = ("error" -> "failed to auth with github") ~ ("code" -> "500")

  object OAuth {
    val clientId = "ea181375c09ee7973608"
    val clientSecret = "3b4435c84bca0ab98dd834fc3bcc83deac460c23"
  }

  serve {
    case "api" :: "github" :: "callback" :: Nil Get req => {
      val response: Option[JValue] = authUser(req)

      val ret = response getOrElse ErrorResponse

      val json = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
      InMemoryResponse(json.data, ("Content-Length", json.data.length.toString) ::("Content-Type", "application/json") :: Nil, Nil, 200)
    }
  }

  def authUser(req: Req): Option[JValue] = try {
    val code = req.param("code").open_!

    println("code = " + code)

    import dispatch._
    val oauthGetAccessToken = url("https://github.com/login/oauth/access_token").POST
      .addQueryParameter("client_id", OAuth.clientId)
      .addQueryParameter("client_secret", OAuth.clientSecret)
      .addQueryParameter("code", code)
      .addQueryParameter("state", "12345")
      .addQueryParameter("redirect_uri", "http://review.kiwi.project13.pl/registered")

    val response = Http(oauthGetAccessToken OK as.String)

    // todo get username, persist "KiwiUser"... use GithubClient

    Some(("response" -> response()))
  } catch {
    case ex: Exception =>
      logger.info("Failed to auth...", ex)
      None
  }

}


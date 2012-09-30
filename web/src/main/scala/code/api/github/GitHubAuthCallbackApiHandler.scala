package code.api.github

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.common.{Logger, Full}
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.InMemoryResponse

object GitHubAuthCallbackApiHandler extends RestHelper with Logger {

  val ErrorResponse = ("name" -> "n/a") ~ ("type" -> "n/a") ~ ("size" -> 0L)

  serve {
    case "api" :: "github" :: "callback" :: Nil Get req => {
      val ret = ErrorResponse

      val json = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
      InMemoryResponse(json.data, ("Content-Length", json.data.length.toString) ::("Content-Type", "application/json") :: Nil, Nil, 200)
    }

    case "api" :: "github" :: "callback" :: Nil Post req => {

      val response: Option[JValue] = authUser(req)

      val ret = response getOrElse ErrorResponse

      val json = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
      InMemoryResponse(json.data, ("Content-Length", json.data.length.toString) ::("Content-Type", "application/json") :: Nil, Nil, 200)
    }
  }

  def authUser(req: Req): Option[JValue] = {
    val body = new String(req.body.open_!)

    println("body = " + body)

    None
  }

}


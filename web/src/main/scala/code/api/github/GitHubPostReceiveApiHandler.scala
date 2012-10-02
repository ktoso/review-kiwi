package code.api.github

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.InMemoryResponse
import com.weiglewilczek.slf4s.Logging
import org.eclipse.egit.github.core.client.GitHubClient
import com.reviewkiwi.model.{ChangeToFetch, KiwiRepository, KiwiUser}
import org.eclipse.egit.github.core.service.RepositoryService
import collection.JavaConversions._
import java.util.{Calendar, Date}

object GitHubPostReceiveApiHandler extends RestHelper with Logging {

  val ErrorResponse = ("error" -> "failed to compute") ~ ("code" -> "500")

  serve {
    case "api" :: "github" :: "post_receive" :: Nil Post req => {
      val response: Option[JValue] = enqueueFetchRequest(req)

      val ret = response getOrElse ErrorResponse

      val json = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
      InMemoryResponse(json.data, ("Content-Length", json.data.length.toString) ::("Content-Type", "application/json") :: Nil, Nil, 200)
    }
  }

  def enqueueFetchRequest(req: Req): Option[JValue] = try {
    val body = req.param("payload").get

    logger.info("Got post_recieve information from GitHub: " + body)

    import net.liftweb.json._
    val payload = parse(body).extract[GithubPostReceivePayload]

    payload.commits foreach { c =>
      import com.foursquare.rogue.Rogue._
      val kiwiRepo = (KiwiRepository where (_.name eqs payload.repository.name) get()).get // todo that's obviously BAAAAAD

      // todo store all?
      ChangeToFetch.createRecord
        .repo(kiwiRepo.id.is)
        .objectId(c.id)
        .pushAuthor(c.author.email)
        .createdAt(Calendar.getInstance)
        .save(true)
    }

    Some("success" -> true)
  } catch {
    case ex: Exception =>
      logger.info("Failed to process github post_recieve push...", ex)
      None
  }
}


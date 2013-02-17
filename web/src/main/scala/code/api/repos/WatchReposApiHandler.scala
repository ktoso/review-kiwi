package code.api.repos

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.json.JsonDSL._
import com.weiglewilczek.slf4s.Logging
import com.reviewkiwi.model.{KiwiRepository, KiwiUser}
import com.reviewkiwi.common.util.UniquifyVerb
import scala.Some
import com.foursquare.rogue.Rogue._
import net.liftweb.json.JsonAST.{JObject, JValue}
import org.bson.types.ObjectId
import java.util.UUID
import net.liftweb.http.InMemoryResponse
import scala.Some

object WatchReposApiHandler extends RestHelper with Logging
with UniquifyVerb {

  val ErrorResponse = ("error" -> "failed to compute") ~ ("code" -> "500")

  serve {
    case "api" :: "repos" :: Nil Get req =>
      val response = getAllReposForUser(req)
      respondOrError(response)

    case "api" :: "repos" :: Nil Post req =>
      val response = tryCreateRepo(req)
      respondOrError(response)

    case "api" :: "repos" :: "pooling" :: Nil Post req =>
      val response = switchPooling(req)
      respondOrError(response)
  }

  def respondOrError(response: Option[JValue]) = {
    val (r: InMemoryResponse, code) = response match {
      case Some(ret) => JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse] -> 200
      case None      => JsonResponse(ErrorResponse).toResponse.asInstanceOf[InMemoryResponse] -> 500
    }

    InMemoryResponse(r.data, ("Content-Length", r.data.length.toString) ::("Content-Type", "application/json") :: Nil, Nil, code)
  }



  case class CreateNewRepoRequest(name: String, url: String, user: String)

  case class SwitchPoolingRequest(github_repo_id: String, pooling: Boolean)

  def tryCreateRepo(req: Req): Option[JValue] = try {
    import net.liftweb.json._

    val request = parse(new String(req.body.open_!)).extract[CreateNewRepoRequest]

    logger.info("Got params to create new repo: [%s] ".format(req.params))

    val user = KiwiUser.find(new ObjectId(request.user)).get

    val newRepo = KiwiRepository.createRecord
      .githubRepoId(UUID.randomUUID.toString)
      .fetchUrl(request.url)
      .name(request.name)
      .save(true)

    user.repos(newRepo.githubRepoId.is.get :: user.repos.is).update

    Some("repo" -> newRepo.asJValue)
  } catch {
    case ex: Exception =>
      logger.info("Unable to create new repository...", ex)
      None
  }

  def switchPooling(req: Req): Option[JValue] = try {
    import net.liftweb.json._

    val parsed = parse(new String(req.body.open_!)).extract[SwitchPoolingRequest]

    logger.info("Switching pooling mode for repo [%s] ".format(parsed.github_repo_id))

    val updatedRepo = KiwiRepository
      .where(_.githubRepoId eqs parsed.github_repo_id)
      .findAndModify(_.fetchUsingPooling setTo parsed.pooling)
      .updateOne(returnNew = true)

    Some("repo" -> updatedRepo.get.asJValue)
  } catch {
    case ex: Exception =>
      logger.info("Failed to change pooling mode...", ex)
      None
  }

  def getAllReposForUser(req: Req): Option[JValue] = try {
    import net.liftweb.json._

    val userId = req.param("u").get
    logger.info("Checking repos for user: %s".format(userId))

    val user = KiwiUser.find(new ObjectId(userId)).get

    val repos = KiwiRepository.where(_.githubRepoId in user.repos.is).fetch()
    val uniques = repos.uniquifyOn(_.fetchUrl.is)

    Some(JArray(uniques.map(_.asJValue)))
  } catch {
    case ex: Exception =>
      logger.info("Failed to get user repos...", ex)
      None
  }
}


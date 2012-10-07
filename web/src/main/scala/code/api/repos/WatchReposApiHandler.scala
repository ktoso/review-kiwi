package code.api.repos

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.json.JsonDSL._
import com.weiglewilczek.slf4s.Logging
import com.reviewkiwi.model.{KiwiRepository, KiwiUser}
import com.reviewkiwi.common.util.UniquifyVerb
import net.liftweb.http.InMemoryResponse
import scala.Some
import com.foursquare.rogue.Rogue._
import net.liftweb.json.JsonAST.{JObject, JValue}

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
    val ret = response getOrElse ErrorResponse

    val json = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
    InMemoryResponse(json.data, ("Content-Length", json.data.length.toString) ::("Content-Type", "application/json") :: Nil, Nil, 200)
  }



  case class CreateNewRepoRequest(name: String, url: String)

  case class SwitchPoolingRequest(github_repo_id: Int, pooling: Boolean)

  def tryCreateRepo(req: Req): Option[JValue] = try {
    import net.liftweb.json._

    val request = parse(new String(req.body.open_!)).extract[CreateNewRepoRequest]
    val user = KiwiUser.findByApiKey(req.param("apiKey").get).get

    val newRepo = KiwiRepository.createRecord
      .githubRepoId(-util.Random.nextInt(900000))
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
    val apiKey = req.param("apiKey").get

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

    val user = KiwiUser.findByApiKey(req.param("apiKey").get).get

    val repos = KiwiRepository.where(_.githubRepoId in user.repos.is).fetch()
    val uniques = repos.uniquifyOn(_.fetchUrl.is)

    Some(JArray(uniques.map(_.asJValue)))
  } catch {
    case ex: Exception =>
      logger.info("Failed to get user repos...", ex)
      None
  }
}


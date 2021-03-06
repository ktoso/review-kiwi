package code.api.github

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.common.{Logger, Full}
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.InMemoryResponse
import com.weiglewilczek.slf4s.Logging
import org.eclipse.egit.github.core.client.{GitHubRequest, GitHubResponse, GitHubClient}
import com.reviewkiwi.model.{KiwiRepository, KiwiRepository$, KiwiUser}
import scalaz.Scalaz._
import org.eclipse.egit.github.core.service.RepositoryService
import collection.JavaConversions._
import org.bson.types.ObjectId

object GitHubAuthCallbackApiHandler extends RestHelper with Logging {

  val AccessTokenResponse = """access_token=(\w+)&token_type=(\w+)""".r

  val ErrorResponse = ("error" -> "failed to auth with github") ~ ("code" -> "500")

  object OAuth {
    val clientId = "ea181375c09ee7973608"
    val clientSecret = "3b4435c84bca0ab98dd834fc3bcc83deac460c23"
  }

  serve {
    case "api" :: "github" :: "callback" :: Nil Get req => {
      val maybeAuthedUserObjectId: Option[ObjectId] = authUser(req)

      maybeAuthedUserObjectId match {
        case Some(userId) =>
          S.redirectTo("/repos/index?u=" + userId)

        case None =>
          val ret = ErrorResponse
          val json = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
          InMemoryResponse(json.data, ("Content-Length", json.data.length.toString) ::("Content-Type", "application/json") :: Nil, Nil, 200)
      }
    }
  }

  def authUser(req: Req): Option[ObjectId] = try {
    val code = req.param("code").open_!

    val response = getGithubAuthTokenResponse(code)

    logger.info("Got auth response from github: " + response)

    response match {
      case AccessTokenResponse(token, tokenType) =>
        val github = new GitHubClient()
        github.setOAuth2Token(token)
        val ghUser = github.getUser

        val repos = importRepositories(github)

        val user = KiwiUser.findOAuthToken(token) getOrElse KiwiUser.createRecord
          .name(ghUser)
          .oauthToken(token)
          .oauthTokenType(tokenType)
          .repos(repos.flatten.toList)
          .save(true)

        Some(user.id.get)

      case _ => None
    }

  } catch {
    case ex: Exception =>
      logger.info("Failed to auth...", ex)
      None
  }


  def getGithubAuthTokenResponse(code: String): String = {
    import dispatch._
    val oauthGetAccessToken = url("https://github.com/login/oauth/access_token").POST
      .addQueryParameter("client_id", OAuth.clientId)
      .addQueryParameter("client_secret", OAuth.clientSecret)
      .addQueryParameter("code", code)
      .addQueryParameter("state", "12345")

    val response = Http(oauthGetAccessToken OK as.String)()
    response
  }

  def importRepositories(github: GitHubClient) = {
    val repositoryService = new RepositoryService(github)
    repositoryService.getRepositories map { repo =>

      // todo findOrUpdate
        val kiwiRepo = KiwiRepository.findByGithubId(repo.getId) getOrElse KiwiRepository.createRecord
          .name(repo.getName)
          .fetchUrl(repo.getCloneUrl)
          .githubRepoId(repo.getId.toString)
          .save(true)

        kiwiRepo.githubRepoId.get
    }
  }
}


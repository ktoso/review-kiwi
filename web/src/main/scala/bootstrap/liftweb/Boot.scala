package bootstrap.liftweb

import net.liftweb._

import common._
import http._
import auth.HttpBasicAuthentication
import sitemap._
import Loc._
import java.io.File

import code.api.github._
import com.reviewkiwi.model.mongo.{Config, MongoConfig, MongoInit}
import code.api.repos.WatchReposApiHandler

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def kiwiInit() {
    val config = Config.readFromProperties()

    MongoInit.init(config)
  }

  def boot() {

    kiwiInit()

    // where to search snippet
    LiftRules.addToPackages("code")

    LiftRules.snippetNamesToSearch.default.set((s:String) => LiftRules.searchSnippetsWithRequestPath(s))

    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index",
      Menu.i("Repos") / "repos" / "index",
      Menu.i("Commits") / "commits" / "index"
    )

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries:_*))

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    

    /* Authentication */
    LiftRules.httpAuthProtectedResource.prepend {
      case (Req( _, _, _)) => Empty
    }

    LiftRules.ajaxPostTimeout = 1000*60 // 60 seconds

    // github apis
    LiftRules.dispatch.append(GitHubAuthCallbackApiHandler)
    LiftRules.dispatch.append(GitHubPostReceiveApiHandler)

    // internal apis
    LiftRules.dispatch.append(WatchReposApiHandler)

  }
}

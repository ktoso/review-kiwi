package bootstrap.liftweb

import akka.config.{Configuration, Config}
import net.liftweb._

import common._
import http._
import auth.HttpBasicAuthentication
import sitemap._
import Loc._
import java.io.File
import tv.yap.mongo.{MongoConfig, MongoInit}
import tv.yap.mysql.{MysqlConfig, MysqlInit}
import tv.yap.model.user._
import org.squeryl.PrimitiveTypeMode._
import tv.yap.model.messaging.{RejectedKeyword, YapPoll}
import tv.yap.model.YapCountry
import code.snippet.ShowcardImageUploadHandler

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def yapInit {
    val location = System.getenv("LIFTED_AKKA_CONF")
    if ( location == null ) {
      throw new Exception("Set the LIFTED_AKKA_CONF environment variable to point to the akka configuration file")
    }
    if(!new File(location).exists() ) {
      throw new Exception("Config file '%s' does not exists".format(location))
    }
    System.setProperty("akka.config",location)
    val rootConfig = Config.config

    MongoInit.init(MongoConfig.fromAkkaConfig(rootConfig))

    MysqlInit.initLift(new MysqlConfig {
      override val rootConfig: Configuration = Config.config
    })

    LiftedConfig.initFromAkkaConfig(rootConfig)
  }

  def boot {

    yapInit

    // where to search snippet
    LiftRules.addToPackages("code")

    LiftRules.snippetNamesToSearch.default.set((s:String) => LiftRules.searchSnippetsWithRequestPath(s))


    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index",
      Menu.i("EPG") / "reports" / "index" submenus (
        Menu("Schedules Count") / "reports" / "schedules-count" ,
        Menu("Providers") / "reports" / "epg-providers"),
      Menu.i("Shows") / "shows" / "list" submenus (
        Menu("Merge shows") / "shows" / "merge",
        Menu("Merge requests status") / "shows" / "merge-requests",
        Menu("for-station") / "shows" / "for-station"  >> Hidden,
        Menu("assets") / "shows" / "assets"  >> Hidden)
    ) ::: YapPoll.menus ::: RejectedKeyword.menus ::: YapCountry.menus:::
    List(
      Menu.i("Rovi") / "rovi" / "index" submenus (
        Menu(" program-show matching ") / "rovi" / "program_show_matching",
          Menu(" social show creator") / "rovi" / "social_show_creator"
      ),
      Menu("Operations") / "operations" / "index",
      Menu(" Runner Events ") / "runner-events" / "index"
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

    LiftRules.authentication = HttpBasicAuthentication("lift") {
      case (username, password, req) => {
        transaction{ ar.YapUser.findByCredentials(username,password)} match {
          case Some(user) if user.isAdmin.is => {
            true
          }
          case _ => false
        }
      }
    }

    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath(
      "show-groups" :: "edit" :: gid :: Nil, "", true, false), GetRequest, http) =>
      RewriteResponse("show-groups" :: "edit" :: Nil,
        Map("id" ->gid))
    }

    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath(
      "shows" :: sid :: "assets" :: Nil, "", true, false), GetRequest, http) =>
        RewriteResponse("shows" :: "assets" :: Nil,
          Map("yap_show_id" ->sid))
    }

    LiftRules.dispatch.append(ShowcardImageUploadHandler)

  }
}

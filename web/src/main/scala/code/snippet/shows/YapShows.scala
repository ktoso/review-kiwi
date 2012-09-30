package code.snippet.shows

import net.liftweb.http.SHtml
import com.foursquare.rogue.Rogue._
import tv.yap.model.messaging.YapShow
import java.util.regex.Pattern
import net.liftweb.http.js.{JsCmd, JsCmds}
import xml.Text
import net.liftweb.common.Loggable
import tv.yap.model.showfeed.YapShowFeedItemContainer
import tv.yap.common.util.NameCleaner
import code.snippet.lib.HasCountrySelector
import tv.yap.model.YapCountry
import tv.yap.model.epg.YapCountrySchedule
import org.joda.time.{DateTimeZone, DateTime}

object YapShows {

  def searchByNamePattern(term: String): Pattern = {
    val searchTerm = term.replaceAll("'", """\\'""") // see https://yap-tv.atlassian.net/browse/PLT-743 not really a part of NameCleaner
    Pattern.compile(".*" + NameCleaner.cleaned(searchTerm) + ".*", Pattern.CASE_INSENSITIVE)
  }
}

class YapShows extends Loggable with HasCountrySelector {
  def setupSearch = {
    <div>
      <span>{Text("Name search: ") ++ SHtml.ajaxText("",(a:String)=>searchByName(a))} | </span>
      <span>{Text("Facebook id search: ") ++ SHtml.ajaxText("",(a:String)=>searchByFacebookId(a))} | </span>
      <span>{SHtml.a(()=>searchRecentIncomplete,Text("Recent incomplete"))} | </span>
      <span>{SHtml.a(()=>searchFacebookWithoutShowcard,Text("FB w/o showcard"))}</span>
    </div>
  }

  def searchByName(term:String): JsCmd = {
    val pattern = YapShows.searchByNamePattern(term)
    renderShows(YapShow where (_.canonicalName matches pattern) fetch(100)) &
      JsCmds.SetHtml("explanation",Text("Shows matching '%s'".format(term)))
  }

  def searchByFacebookId(facebookId:String): JsCmd = {
    val completeContainers = YapShowFeedItemContainer.fetchCompleteContainers(
      YapShowFeedItemContainer where (_.facebookIds contains facebookId) fetch())

    val shows = YapShow where (_.resourceId in completeContainers.flatMap(_.allShowIds)) fetch()

    renderShows(shows) &
      JsCmds.SetHtml("explanation",Text("Shows mapped to facebook id '%s'".format(facebookId)))
  }

  def searchRecentIncomplete = {
    renderShows(YapShow where (_.hashtags eqs "")
      and (_.programType in "SeriesMaster"::"Season of a Series"::Nil)
      orderDesc(_.resourceId) fetch(100)) &
      JsCmds.SetHtml("explanation",Text("Recent shows incomplete"))
  }

  def searchFacebookWithoutShowcard {
    renderShows(YapShow.where(_.facebookPageId exists(true)) and (_.imageUrl exists(false))
      orderDesc(_.resourceId) fetch(100)) &
      JsCmds.SetHtml("explanation",Text("With FB, without showcards"))

  }

  def handleCountrySelected(chosen:Option[YapCountry]):JsCmd = {
    chosen.map { country=> {
      val countrySchedule = new YapCountrySchedule(country)
      val now = new DateTime(DateTimeZone.UTC)
      val showIds = countrySchedule.findPrimeTimeShowIds(now)
      renderShows(YapShow.where(_.resourceId in(showIds)) orderDesc(_.resourceId) fetch(400)) &
        JsCmds.SetHtml("explanation",Text("In country " + country.code.is))

    }}.getOrElse(JsCmds.SetHtml("explanation",Text("Select a country")))
  }

  private def renderShows(shows: Seq[YapShow]): JsCmd = new YapShowsListRenderer(shows).doRender()
}

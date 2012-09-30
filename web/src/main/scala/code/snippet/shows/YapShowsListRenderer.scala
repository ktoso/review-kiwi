package code.snippet.shows

import net.liftweb.http.{Templates, S, SHtml}
import com.foursquare.rogue.Rogue._
import tv.yap.model.messaging.YapShow
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.{JsCmds, JsCmd}
import xml.{NodeSeq, Text}
import code.lib.{YapApiUtil, YapUrlUtil}
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.http.js.jquery.JqJsCmds.{Unblock, ModalDialog}
import net.liftweb.util._
import Helpers._
import scala.collection.JavaConversions._
import akka.dispatch.Future
import tv.yap.model.showfeed.{YapShowFeedItemContainerComplete, YapShowFeedItemContainer}
import com.weiglewilczek.slf4s.Logging
import tv.yap.model.cache.YapCountriesCache
import code.snippet.lib.HasCountrySelector
import tv.yap.model.YapCountry
import scalaz.Scalaz._
import org.apache.http.client.utils.URIBuilder
import code.snippet.ShowcardImageUpload
import java.text.DateFormat
import net.liftweb.http.js.JE.JsObj

class YapShowsListRenderer(shows: Seq[YapShow]) extends Logging {
  val ContainerId = "shows"
  val NoValuePlaceholder = "-"

  private lazy val countriesCache = new YapCountriesCache(defaultCountry = None).refresh()

  def ops(show:YapShow) = {
    def cssClass="show_op"
    val assetsUrl = "/shows/%d/assets".format(show.resourceId.is)
    <a href={YapUrlUtil.showEdit(show)} class={cssClass} target="_blank">admin</a> ++
      <span> | </span> ++
      <a href={YapUrlUtil.fancyEdit(show)} class={cssClass}  target="_blank">fancy</a>
        <span> | </span> ++
      SHtml.a(()=>editShow(show),scala.xml.Unparsed("&#9997;"),"class"->cssClass,"title"->"Edit") ++
      <span> | </span> ++
      SHtml.a(()=>advancedView(show),scala.xml.Unparsed("&#9763;"),"class"->cssClass,"title"->"Advanced View") ++
      <span> | </span> ++
      <a href={assetsUrl} class={cssClass}  target="_blank" title="Assets">&#9635;</a>
  }

  /*
   * Generate a span + css class for links and use in conjuction with javascript code to open in iframe.
   * This is not active yet because of permission issues. If we re-implement enough functionality
   * in the lifted project we may not have to rely at all on the older admins.
   */
  def opsAsSpan(show:YapShow) = {
    def cssClass="show_op"
    val assetsUrl = "/shows/%d/assets".format(show.resourceId.is)
    <span href={YapUrlUtil.showEdit(show)} class={cssClass}>admin</span> ++
      <span> | </span> ++
      <span href={YapUrlUtil.fancyEdit(show)} class={cssClass}>fancy</span>
        <span> | </span> ++
      SHtml.a(()=>editShow(show),Text("edit"),"class"->cssClass) ++
      <span> | </span> ++
      <span href={assetsUrl} class={cssClass}>assets</span>
  }

  private def editShow(show:YapShow) = {
    var fbNewId = ""
    var fbNewCountry: Option[YapCountry] = None

    val template = Templates.findRawTemplate("shows"::"_edit"::Nil,S.locale)
    template.map(t=>{
      val currentContainers = YapShowFeedItemContainer where (_.yapShowIds contains show.resourceId.is) fetch()
      val currentFacebookIds = currentContainers.map(c => {
          (".fb_current_row_id * " #> c.facebookIds.is.mkString(", ")) &
          (".fb_current_row_country * " #> c.yapCountryIds.is.map(countriesCache(_).name.is).mkString(" "))
      })

      val out = (
        "#show_hashtags *+" #> SHtml.text(show.hashtags.is,s => if (s != "") { show.hashtags(s) }) &
          "#show_main_show *+" #> SHtml.text(show.mainShowInSeries.is.getOrElse(NoValuePlaceholder).toString, setMainShow(show, _)) &
          "#show_thumbnails *+" #> SHtml.ajaxButton("process thumbnails",()=>processThumbnails(show)) &
//          "#show_id" #> show.resourceId.is &
          "#showcard_upload" #> new ShowcardImageUpload(show.resourceId.is).form &
          "#show_save" #> SHtml.ajaxSubmit("Save",()=>{saveShow(show) & Unblock}) &
          "#show_cancel" #> SHtml.ajaxButton("Cancel",()=>{Unblock}) &
          ".fb_current_row *" #> currentFacebookIds &
          "#fb_id *+" #> SHtml.text("", fbNewId = _) &
          "#fb_country *+" #> (new HasCountrySelector {
            def handleCountrySelected(chosen: Option[YapCountry]) = { fbNewCountry = chosen; JsCmds.Noop }
          }).countriesAjaxSelect &
          "#fb_associate *+" #> SHtml.ajaxSubmit("Associate",()=>{ associateShowWithFacebookId(show, fbNewId, fbNewCountry) & Unblock})
        )(t)
      ModalDialog(out)
    }) openOr Alert("Couldn't find shows/_edit template")
  }

  private def advancedView(show:YapShow) = {
    val template = Templates.findRawTemplate("shows"::"_advanced"::Nil,S.locale)
    template.map(t=>{
      val out = (
        "#show_cancel" #> SHtml.ajaxButton("Close",()=>{Unblock}) &
        "#show_content *+" #> (
          <tr><td>Resource ID</td><td>{show.resourceId.is}</td></tr> ++
          <tr><td>Last Air Date</td><td>{show.lastAirDate.get.map(String.format("%1$tm %1$te,%1$tY",_)).getOrElse("-")}</td></tr> ++
          <tr><td>Program Type</td><td>{show.programType}</td></tr> ++
          <tr><td>Image Url</td><td><a href={show.imageUrl.is.getOrElse("")} target="_blank">{scala.xml.Unparsed(show.imageUrl.is.getOrElse(""))}</a></td></tr> ++
          <tr><td>Network</td><td>{show.network.is}</td></tr> ++
          <tr><td>All Topics</td><td>{show.allTopics("")}</td></tr> ++
          <tr><td>Auto Prefix</td><td>{show.autoPrefix}</td></tr> ++
          <tr><td>Overrides</td><td>{show.overrides.is.map(o=>o.name.is+"="+o.imageTag.is).mkString(",")}</td></tr> ++
          <tr><td>Rovi Ids</td><td>{show.roviIds.is.mkString(", ")}</td></tr>
          )
        )(t)
      ModalDialog(out,JsObj(("width","600px")))
    }) openOr Alert("Could not find shows/_advanced template")

  }

  def onTop(one:String,two:Option[String]):NodeSeq = {
    val expanded = two.getOrElse("")
    if (one.isEmpty) <td>{expanded}</td>
    else if (expanded.isEmpty ) <td>{one}</td>
    else <td>{one}<br />{expanded}</td>
  }

  def doRender(): JsCmd = {
    val containers = YapShowFeedItemContainer where (_.yapShowIds in shows.map(_.resourceId.is)) fetch()
    val completeContainers = YapShowFeedItemContainer.fetchCompleteContainers(containers)

    // A show may have multiple containers for different languages
    val containersByShowId: Map[Int, List[YapShowFeedItemContainerComplete]] = completeContainers
      .toList
      .flatMap(cc => cc.allShowIds.map(_ -> cc))
      .groupBy(_._1)
      .map(p => p._1 -> p._2.map(_._2))

    // containers -> string: fb1, fb2; fb3, fb4 (PL); fb5 (DE)
    def containerFacebookIds(containers: Seq[YapShowFeedItemContainerComplete]): String = {
      containers.map(c => {
        val base = c.container.facebookIds.is.mkString(", ")
        val countries = c.container.yapCountryIds.is
        val suffix = if (countries.size > 0) {
          " (" + countries.map(countriesCache.apply(_).code.is).mkString(" ") + ")"
        } else ""

        base + suffix
      }).mkString("; ")
    }

    val content = shows flatMap ( show=>
      <tr>
        <td>{show.resourceId.is}</td>
        <td>{show.name.is}</td>
        {onTop(show.hashtags.is,containersByShowId.get(show.resourceId.is).map(containerFacebookIds(_)))}
        <td>{ops(show)}</td>
      </tr>
      )
    JqJsCmds.JqSetHtml(ContainerId,content) &
      JqJsCmds.Show("shows_container")
  }

  private def associateShowWithFacebookId(show: YapShow, fbId: String, country: Option[YapCountry]) = {
    logger.info("Associating %s (%d) with facebook id %s for %s".format(show.name.is, show.resourceId.is, fbId, country))

    if (fbId /== "") {
      val uriBuilder = new URIBuilder("/yap_show/%d/associate_with_facebook".format(show.resourceId.is))
      uriBuilder.addParameter("fb_id", fbId)
      country.foreach { theCountry => uriBuilder.addParameter("country_id", theCountry.id.is.toString) }

      YapApiUtil.callYapApi(uriBuilder.build().toString,
        "POST",
        "Associated with facebook id %s for country %s".format(fbId, country.map(_.name.is).getOrElse("default")),
        "Cannot associate with facebook id: ")
    }

    doRender()
  }

  private def saveShow(show: YapShow) = {
    show.update

    S.notice("Show saved")
    doRender()
  }

  private def setMainShow(show: YapShow, value: String) {
    if (value != NoValuePlaceholder) {
      show.mainShowInSeries(value.toInt)
    }
  }

  private def processThumbnails(show:YapShow) = {
    Future {
      val command = "/app/bin/process-assets.sh" :: show.resourceId.toString() :: Nil
      try {
        new ProcessBuilder(command).start()
      } catch {
        case e: Exception => logger.error(e.getMessage)
      }
    }
    JqJsCmds.AppendHtml("#show_thumbnails",Text("+"))
  }
}

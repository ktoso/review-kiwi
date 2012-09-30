package code.snippet.reports
import tv.yap.model._
import com.foursquare.rogue.Rogue._
import net.liftweb.http.{Templates, S, SHtml}
import net.liftweb.common.{Full, Box, Empty}
import net.liftweb.http.js.JsCmds.{Alert, SetHtml}
import xml.{Elem, Text, NodeSeq}
import net.liftweb.http.js.jquery.JqJsCmds
import net.liftweb.http.js.{JsCmd, JsCmds, JE}
import net.liftweb.util._
import Helpers._
import net.liftweb.http.js.jquery.JqJsCmds.{Unblock, ModalDialog}
import tv.yap.common.util.safe.SafeInt
import com.mongodb.WriteConcern
import code.snippet.lib.HasCountrySelector


class EpgProviders extends HasCountrySelector {

  val pageSize = 10

  var page = 0

  val containerId = "provider_row"

  val stationsId = "stations"

  var chosenCountry:Option[YapCountry] = Some(countries.head)

  def handleCountrySelected(chosen:Option[YapCountry]):JsCmd = {
    chosenCountry = chosen
    val mainLineupName = chosenCountry.flatMap(c=>c.mainLineup).
      map(lineup=> lineup.providerName+" "+lineup.locationName)

    page = 4

    // display the country, reset the container content and fetch the first pages
    SetHtml("selected_country",Text(chosenCountry.map(_.name.is).getOrElse("-"))) &
      SetHtml("main_lineup",Text(mainLineupName.getOrElse("-"))) &
      SetHtml(containerId,Text("")) &
      JqJsCmds.Show("providers_container") &
      list(0) & list(1) & list(2) & list(3) & list(4)
  }

  /*
   * Send javascript to setup the infinite scroll.
   */
  def setupScroll = {
    <span></span> ++ JsCmds.Script(JE.JsRaw(
      """$(window).scroll(function(){
  if($(window).scrollTop() == $(document).height() - $(window).height()) {"""+
        SHtml.jsonCall(containerId, process)._2.toJsCmd+"""
  }
})""").cmd)
  }

  def process(in:Any) =  {
    page += 1
    list(page)
  }

  private def renderStations(lineup:YapLocalLineup,sub:Lineup) = {
    def url(station:Station) = {
      val stationId:Int = station.yapTmsStationId.is.getOrElse(-1)
      val device:String = sub.device.is
      val lineupId:String = lineup.id.is.toString
      "/shows/for-station.html?epg_ctry=%s&yap_local_lineup_id=%s&device=%s&station_id=%d".format(
        chosenCountry.get.code.is, lineupId, device,stationId)
    }
    val content = sub.stations.is.sortBy(s=>SafeInt(s.channel.is)).flatMap(st => <tr><td>{st.channel}</td><td>{st.callSign}</td><td> <a href={url(st)}>{st.name}</a></td></tr>)

    content
  }

  private def displayStationsPopup(lineup:YapLocalLineup,sub:Lineup):JsCmd = {
    val template = Templates.findRawTemplate("reports"::"_stations"::Nil,S.locale)
    template.map(t=>{
      val out = (
        "#stations_content" #> renderStations(lineup,sub) &
        "#stations_close" #> SHtml.ajaxButton(Text("Close"), () =>Unblock )
        )(t)
      ModalDialog(out)
    }).getOrElse(Alert("Cannot process the _stations template"))
  }

  private def renderLineupLink(lineup:YapLocalLineup,sub:Lineup) = {
    SHtml.a(()=>displayStationsPopup(lineup,sub), <span>device {sub.device.is} | stations {sub.stations.is.length}</span><br/>)
  }

  // Generate DOM id for the Operation cell of a lineup
  private def opId(lineup:YapLocalLineup) = "op"+lineup.id.is.toString

  // Name of the operation based on the current hidden state
  private def opName(isHidden: Boolean) = if (isHidden) "Show" else "Hide"

  // Toggle the lineup hide flag and update the Operation cell
  private def lineupHideToggle(lineup:YapLocalLineup) = {
    val _new = !lineup.hidden.is
    lineup.hidden(_new).save
    JsCmds.SetHtml(opId(lineup),Text(opName(_new)))
  }

  private def makeMainForCountry(lineup:YapLocalLineup) {
    YapCountry where(_.id eqs lineup.yapCountryId.is) modify(_.mainLineupId setTo(lineup.id.is)) updateOne(WriteConcern.SAFE)
    chosenCountry.get.mainLineupId(lineup.id.is)
  }

  def showMainForCountry(lineup:YapLocalLineup):NodeSeq = {
    if (chosenCountry.get.mainLineupId.is == lineup.id.is)
      <span>Current main</span>
    else
      <span>Make main</span>
  }

  def list(page:Int)  = {
    val lineups = (YapLocalLineup where(_.yapCountryId eqs chosenCountry.get.id.is)
      limit(pageSize) skip(pageSize*page) orderAsc(_.providerName) fetch())
    val content = lineups.flatMap { lineup =>
      <tr>
        <td class="name">{lineup.providerName.is}</td>
        <td class="location">{lineup.locationName.is.getOrElse("-")}</td>
        <td class="devices">{lineup.lineups.is.flatMap(renderLineupLink(lineup, _))}</td>
        <td class="flags">{operationsFor(lineup)}</td>
      </tr>
    }
    content
    JqJsCmds.AppendHtml(containerId,content)
  }

  def operationsFor(lineup: YapLocalLineup): NodeSeq = {
    if (lineup.deleted.is) {
      Text("Deleted")
    } else {
      val myId = opId(lineup)
      val hideOrShow = <span id={myId}> {opName(lineup.hidden.is)} </span>
      
      SHtml.a(() => lineupHideToggle(lineup), hideOrShow) ++ 
      <span> | </span> ++
      SHtml.a(() => makeMainForCountry(lineup),showMainForCountry(lineup))
    }
  }
    
  // Handle the stations popup
  def stationsPopup(in: NodeSeq) = {
    bind("stations", in,
      "close" -> ((b: NodeSeq) => SHtml.ajaxButton(b, () =>Unblock )))
  }
}


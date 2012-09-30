package code.snippet.shows

import net.liftweb.http.{Templates, SHtml, S}
import com.foursquare.rogue.Rogue._
import tv.yap.model.{Station, YapLocalLineup, YapSchedule, YapCountry}
import tv.yap.model.epg.EpgUtil
import net.liftweb.util._
import Helpers._
import xml.{Text, NodeSeq}
import net.liftweb.http.js.JsCmds.Alert
import code.lib.YapUrlUtil
import tv.yap.model.messaging.YapShow
import net.liftweb.http.js.jquery.JqJsCmds.{ModalDialog, Unblock}
import net.liftweb.common.Full
import org.bson.types.ObjectId


class ForStation {

  def stationsList:NodeSeq = {
    val stations = for (
      lineup_id <- S.param("yap_local_lineup_id");
      device <- S.param("device");
      countryCode<- S.param("epg_ctry");
      currentStationId <- S.param("station_id");
      localLineup <- YapLocalLineup where (_.id eqs new ObjectId(lineup_id)) fetch(1) headOption;
      forDevice <- localLineup.lineups.is.find(l=>l.device.is == device);
      val stations = forDevice.stations.is
    ) yield {
      def onSelect(selected:String) = {
        val selectedId = selected.toInt
        S.redirectTo("/shows/for-station.html?epg_ctry=%s&yap_local_lineup_id=%s&device=%s&station_id=%d".
          format(countryCode,lineup_id,device,selectedId))

      }
      SHtml.ajaxSelect(
        stations.map{(c:Station)=>(c.yapTmsStationId.is.get.toString,c.name.is.getOrElse("-"))},
        Full(currentStationId),
        onSelect
      )
    }
    stations.getOrElse(Text(""))
  }

  def list = {
    val stationId = S.param("station_id").getOrElse("station_id parameter missing" ).toInt
    listStationId(stationId)
  }

  def listStationId(stationId:Int) = {
    val countryCode = S.param("epg_ctry").getOrElse("epg_ctry parameter missing")
    val country = YapCountry where (_.code eqs countryCode ) fetch(1) head
    val scheduleMeta = EpgUtil.scheduleMetaFor(country)
    val schedules = scheduleMeta where(_.yapTmsStationId eqs stationId) orderAsc(_.startTime) fetch()
    val shows:Map[Int,YapShow] = YapShow where(_.resourceId in schedules.flatMap(_.yapShowId.is)) fetch () map {s=> (s.resourceId.is,s)} toMap

    ".schedule_row" #> schedules.map( schedule =>
      ".start_time *+" #> schedule.startTime.is.get.getTime.toString &
      ".name *+" #> schedule.showName.is &
      ".episode_name *+" #> schedule.episodeName.is &
      ".program_type *+" #> schedule.programType.is &
      ".genres *+" #> schedule.genres.is.map(g=>g.genre).mkString(",") &
      ".ops *+" #> ops(schedule,shows)
    )  & "#selected" #> stationId
  }

  private def ops(schedule:YapSchedule,shows:Map[Int,YapShow]):NodeSeq = {
    schedule.yapShowId.is match {
      case Some(yapShowId) => {
        val show = shows(yapShowId)

        <a href={"http://www.yap.tv/show/"+schedule.yapShowId.is}>View</a>
        <span> | </span>
        <a href={YapUrlUtil.showEdit(show)}>Old Admin</a>
      }
      case None => SHtml.a(() => createShow(schedule), Text("Create"))
    }
  }

  private def createShow(schedule:YapSchedule) = {
    val template = Templates.findRawTemplate("shows"::"_create"::Nil,S.locale)
    template map { t =>
      val out = (
        "#show_save" #> SHtml.ajaxButton("Save",()=>{Unblock & Alert("Not implemented")}) &
        "#show_cancel" #> SHtml.ajaxButton("Cancel",()=>{Unblock})
      )(t)

      ModalDialog(out)
    } openOr Alert("Couldn't find shows/_edit template")
  }
}

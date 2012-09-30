package code.comet

import net.liftweb.http._
import js.{JsCmds, JsCmd}
import xml.NodeSeq
import akka.actor.Actor
import akka.actor.Actor._
import scalaz.Scalaz._
import tv.yap.rovi.shows.{ScheduleShowIdsFixer, DuplicateYapShowResolver}
import tv.yap.model.messaging.YapShow
import tv.yap.model.YapRoviProgram
import tv.yap.ModelServices
import net.liftweb.common.Logger
import tv.yap.common.util.DoToVerb
import code.lib.js.YapJsCmd
import scala.Some
import code.snippet.shows.YapShows
import com.weiglewilczek.slf4s
import slf4s.CometActorAwareLogger
import tv.yap.model.operations.ShowMergeRequest

/** This actor is responsible for interacting with the UI, and it delegates work to the worker akka actor */
class ShowMergeCometActor extends CometActor with Logger {

  private var selectedMergeIntoShow: Option[YapShow] = None
  private var selectedMergeAndDeleteShow: Option[YapShow] = None

  import code.presenter.YapShowPresenter._

  override def mediumPriority = {

    case SelectMergeIntoShow(show) =>
      info("Registered: " + show.name + " to be merged into")
      selectedMergeIntoShow = show.some

      partialUpdate(JsCmds.SetHtml("show-into-name", show.present(_.resourceId, _.name, _.presentOldAdminLink, _.presentAssetsLink).styledStrong))

    case SelectMergeAndDeleteShow(show) =>
      info("Registered: " + show.name + " to be merged and removed")
      selectedMergeAndDeleteShow = show.some
      partialUpdate(JsCmds.SetHtml("show-delete-name", show.present(_.resourceId, _.name, _.presentOldAdminLink, _.presentAssetsLink).styledStrong))

    case ClearSelectedShows =>
      selectedMergeAndDeleteShow = None
      selectedMergeIntoShow = None
      partialUpdate(JsCmds.SetHtml("show-delete-name", <span></span>))
      partialUpdate(JsCmds.SetHtml("show-into-name", <span></span>))

    case MergeSelectedShows =>
      (selectedMergeIntoShow, selectedMergeAndDeleteShow) match {
        case (Some(keep), Some(rm)) =>
          storeMergeShowsRequest(keep, rm)
          this ! ClearSelectedShows
          this ! "Shows [%s] and [%s] have been added to the merge queue. You can monitor them bellow on this page."
            .format(keep.name, rm.name)

        case _ => partialUpdate(JsCmds.Alert("No Shows to merge selected!"))
      }

    case msg: String => partialUpdate(YapJsCmd.AppendText("merge-progress", msg))
  }

  def storeMergeShowsRequest(store: YapShow, remove: YapShow) = {
    ShowMergeRequest.create(store, remove).save(safe = true)
  }

  override def render = {
    "#select-shows" #> showSelectionFormOrPleaseWait
  }

  def showSelectionFormOrPleaseWait: NodeSeq = {
    ShowMergeSelectionForm.get(this)
  }
}

sealed case class MergeShows(mergeIntoShow: YapShow, deleteShow: YapShow)
sealed case class SelectMergeAndDeleteShow(show: YapShow)
sealed case class SelectMergeIntoShow(show: YapShow)
case object MergeSelectedShows
case object ClearSelectedShows

object ShowMergeSelectionForm {

  val LimitForQueryShowsByName = 100

  def get(comet: CometActor) = {
    SHtml.ajaxForm {
      <div>
        <span>Show to merge (and <b>delete</b>):</span> {
          SHtml.ajaxText("", searchShowByName("to-delete-merge-shows", _, comet, SelectMergeAndDeleteShow.apply _))
        }
        <div id="to-delete-merge-shows"><!-- Will contain found shows, ready to be selected --></div>
      </div> ++
      <div>
        <span>Show to merge with:</span> {
          SHtml.ajaxText("", searchShowByName("target-merge-shows", _, comet, SelectMergeIntoShow.apply _))
        }
        <div id="target-merge-shows"><!-- Will contain found shows, ready to be selected --></div>
      </div> ++
      SHtml.ajaxButton("Merge!", () => { comet ! MergeSelectedShows; JsCmds.Noop } ) ++
      SHtml.ajaxButton("Clear!", () => { comet ! ClearSelectedShows; JsCmds.Noop } )
    }
  }

  def searchShowByName(resultsNodeId: String, nameFragment: String, comet: CometActor, selectMessage: (YapShow) => Any): JsCmd = {
    if(nameFragment.trim.isEmpty) return JsCmds.Noop

    val pattern = YapShows.searchByNamePattern(nameFragment)

    import com.foursquare.rogue.Rogue._
    val shows = YapShow where (_.name matches pattern) fetch (limit = LimitForQueryShowsByName)

    import code.presenter.YapShowPresenter._

    val selectShowButton = (show: YapShow) => {
      SHtml.ajaxButton("Select", () => { comet ! selectMessage(show); JsCmds.Noop })
    }

    JsCmds.SetHtml(resultsNodeId,
      shows.presentAsUl(_.resourceId.is, _.name.is, _.presentOldAdminLink, _.presentAssetsLink, selectShowButton)
    )
  }
}
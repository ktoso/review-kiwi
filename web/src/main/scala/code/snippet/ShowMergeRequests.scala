package code.snippet

import net.liftweb.util._
import Helpers._
import com.foursquare.rogue.Rogue._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeZone, DateTime}
import tv.yap.model.operations.{ShowMergeRequest, RunnerEvent}
import code.presenter.ShowMergeRequestPresenter

class ShowMergeRequests {

  val SimpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC)

  import ShowMergeRequestPresenter._

  def queue = {
    val all = ShowMergeRequest.findAllPending()

    cssSelFor(all)
  }

  def finished = {
    val all = ShowMergeRequest.findAllFinished(limit = 20)

    cssSelFor(all)
  }

  def cssSelFor(all: List[ShowMergeRequest]): CssSel =
    ".merge-requests" #> all.map { request =>
      ".completed *+" #> request.presentStatusIcon &
      ".in-progress *+" #> request.presentInProgressIcon &
      ".attempts *+" #> request.mergeAttempts.is &
      ".merge-details *+" #> mergeDetailsFor(request) &
      ".created-at *+" #> <span>{SimpleDateFormat.print(new DateTime(request.createdAt.is))}<br/>{request.presentTimeSinceCreatedAt}</span> &
      ".updated-at *+" #> <span>{SimpleDateFormat.print(new DateTime(request.updatedAt.is))}<br/>{request.presentTimeSinceUpdatedAt}</span>
    }

  def mergeDetailsFor(request: ShowMergeRequest) = {
    " " + request.mergeAndDeleteShowId.is + " -> " + request.mergeIntoShowId.is
  }
}

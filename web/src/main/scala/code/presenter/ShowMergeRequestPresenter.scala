package code.presenter

import tv.yap.model.messaging.YapShow
import xml._
import tv.yap.model.operations.ShowMergeRequest
import org.joda.time.format.PeriodFormat
import org.joda.time.{Minutes, Period, DateTime}
import xml.Text

object ShowMergeRequestPresenter extends StyleablePresenter {

  implicit def mergeRequestPresenter(s: ShowMergeRequest) = new ShowMergeRequestPresenter(s)
}

class ShowMergeRequestPresenter(request: ShowMergeRequest) {

  def presentStatusIcon: NodeSeq =
    if (request.completed.is)
      <img alt="completed"/> % Attribute(None, "src", Text("/images/icons/accept.png"), Null)
    else if(request.skipped.is)
        <img alt="failed / skipped"/> % Attribute(None, "src", Text("/images/icons/cancel.png"), Null)
    else
      <span></span>

  def presentInProgressIcon: NodeSeq =
    if (request.inProgress.is)
      <img alt="in-progress"/> % Attribute(None, "src", Text("/images/icons/arrow_merge.png"), Null)
    else
      <img alt="waiting..."/> % Attribute(None, "src", Text("/images/icons/control_pause.png"), Null)

  def presentTimeSinceUpdatedAt: NodeSeq = {
    val then = new DateTime(request.updatedAt.is)
    val now = new DateTime
    presentTimeSince(then, now)
  }

  def presentTimeSinceCreatedAt: NodeSeq = {
    val then = new DateTime(request.createdAt.is)
    val now = new DateTime
    presentTimeSince(then, now)
  }

  private def presentTimeSince(one: DateTime, two: DateTime) =  {
    val period = Minutes.minutesBetween(one, two)
    <span>{PeriodFormat.getDefault.print(period)} ago</span>
  }

}

package code.snippet

import com.foursquare.rogue.Rogue._
import tv.yap.model.rovi.{CandidateStatus, ScheduleMatchCandidate}
import tv.yap.model.messaging.YapShow
import tv.yap.model.YapRoviProgram
import net.liftweb.util._
import net.liftweb.http._
import Helpers._
import SHtml._
import tv.yap.ModelServices

class ScheduleMatchCandidates extends StatefulSnippet with ModelServices {
  override def dispatch = {case _ => list}

  def list = {
    val all = ScheduleMatchCandidate where (_.status eqs CandidateStatus.Pending) fetch()

    val showIds = all.map(_.yapShowId.is).toSet
    val showNamesById: Map[Int, String] = Map() ++ (YapShow where (_.resourceId in showIds) select ((_.resourceId), (_.name)) fetch())

    val programIds = all.map(_.roviProgramId.is).toSet
    val programNamesById: Map[Int, String] = Map() ++ (YapRoviProgram where (_.roviId in programIds) select ((_.roviId), (_.showName)) fetch())

    ".total *+" #> all.size &
    ".candidate_row" #> all.map{ candidate =>
      val yapShowId = candidate.yapShowId.is
      val roviProgramId = candidate.roviProgramId.is

      ".show_id *+" #> yapShowId &
        ".show_name *+" #> showNamesById.get(yapShowId).getOrElse("???") &
        ".program_id *+" #> roviProgramId &
        ".program_name *+" #> programNamesById.get(roviProgramId).getOrElse("???") &
        ".accept" #> onSubmitUnit(() => accept(candidate)) &
        ".reject" #> onSubmitUnit(() => reject(candidate))
    }
  }

  private def accept(candidate: ScheduleMatchCandidate) {
    candidate.accept()
    invokeBinder(candidate)
    addNotice("Accepted binding between %s and %s", candidate)
  }

  private def reject(candidate: ScheduleMatchCandidate) {
    candidate.reject()
    addNotice("Rejected binding between %s and %s", candidate)
  }

  private def addNotice(msg: String, candidate: ScheduleMatchCandidate) {
    val yapShowName = YapShow.findByResourceId(candidate.yapShowId.is).get.name.is
    val programName = YapRoviProgram.findByRoviId(candidate.roviProgramId.is).get.showName.is

    S.notice(msg.format(yapShowName, programName))
  }

  private def invokeBinder(candidate: ScheduleMatchCandidate) {
    val yapShow = YapShow.findByResourceId(candidate.yapShowId.is).get
    val program = YapRoviProgram.findByRoviId(candidate.roviProgramId.is).get

    showAndProgramLinker.linkShowAndProgram(yapShow, program)
  }
}

package code.snippet

import lib.HasCountrySelector
import net.liftweb.http.SHtml
import net.liftweb.common.Loggable
import net.liftweb.util._
import Helpers._
import net.liftweb.http.js.jquery.JqJsCmds
import xml.{Text, NodeSeq}
import tv.yap.ModelServices
import tv.yap.model.{YapCountry, YapProgram}
import tv.yap.model.messaging.YapShow
import net.liftweb.http.js.{JsCmds, JsCmd}
import code.presenter.YapShowPresenter

class RoviSocialShowCreator extends Loggable with ModelServices {


  def setupStart = {
    var showName = ""
    var facebookPageUrl = ""

    "#start_show_name *+" #> SHtml.text("", showName = _, "size" -> "40") &
      "#start_facebook_page_url *+" #> SHtml.text("", facebookPageUrl = _, "size" -> "40") &
      "#start_submit" #> SHtml.ajaxSubmit("Try to create or associate",
        () => jsResult(tryCreateOrAssociate(showName, facebookPageUrl)))
  }

  private def tryCreateOrAssociate(showName: String, facebookPageUrl: String): NodeSeq = {
    val facebookId = facebookIdReader.fromPageUrl(facebookPageUrl).getOrElse({
      return Text("Cannot get facebook id for: " + facebookPageUrl)
    })

    val programs = matchingYapProgramFinder.byName(showName)

    programs.size match {
      case 0 => Text("Cannot find rovi program for: " + showName)
      case 1 => {
        val program = programs(0)
        program.yapShowId.is match {
          case None => createNewShow(program, facebookId)._2
          case Some(yapShowId) => associateWithExistingShow(facebookId, yapShowId)
        }
      }
      case _ => handleMultiplePrograms(programs, facebookId)
    }
  }

  private def createNewShow(program: YapProgram, facebookId: String): (YapShow, NodeSeq) = {
    val newShow = yapShowFromProgramCreator.createShowFrom(program).get
    showAndFacebookIdAssociator.associate(newShow, facebookId,None)

    (newShow, messageShowResult("New show %s (%d) has been created".format(newShow.name.is, newShow.resourceId.is), newShow))
  }

  private def associateWithExistingShow(facebookId: String, yapShowId: Int): NodeSeq = {
    val yapShow = YapShow.findByResourceId(yapShowId).get

    if (showAndFacebookIdAssociator.isAssociated(yapShow, facebookId)) {
      messageShowResult("Show %s (%d) is already associated with facebook id %s".format(yapShow.name.is, yapShow.resourceId.is, facebookId), yapShow)
    } else {
      showAndFacebookIdAssociator.associate(yapShow, facebookId, None)
      messageShowResult("Associated facebook id %s and existing show %s (%d)".format(facebookId, yapShow.name.is, yapShow.resourceId.is), yapShow)
    }
  }

  private def handleMultiplePrograms(programs: Seq[YapProgram], facebookId: String): NodeSeq = {
    val extInfos = programs.map(yapProgramExtendedInformationGatherer.forProgram(_))

    val choiceResults = new collection.mutable.HashMap[Int, Boolean]()
    val choices = extInfos.zipWithIndex map { case (extInfo, idx) =>
      <tr>
        <td>{SHtml.checkbox(value = false, func = choiceResults.put(idx, _))}</td>
        <td>{extInfo.program.showName.is} ({extInfo.program.roviId.is})</td>
        <td>{extInfo.showName.getOrElse("-")} {extInfo.showId.map("(" + _ + ")").getOrElse("")}</td>
        <td>{extInfo.allDescriptions}</td>
        <td>{extInfo.allGenres}</td>
        <td>{extInfo.exampleAsset.map(a => <a href={a}>Asset</a>).getOrElse(Text("-"))}</td>
      </tr>
    }

    <form method="POST" class="lift:form.ajax">
      Multiple programs matching the given name found. Please select the programs from which the new show should be
      created (you can select multiple).

      <table>
        <tr>
          <th>Select?</th>
          <th>Program info</th>
          <th>Associated show info</th>
          <th width="50%">Descriptions</th>
          <th>Genres</th>
          <th>Example asset</th>
        </tr>
        {choices}
      </table>

      {SHtml.ajaxSubmit("Create show from selected programs",
        () => jsResult(createShowFromPrograms(programs, choiceResults.toMap, facebookId)))}
    </form>
  }

  def createShowFromPrograms(programs: Seq[YapProgram], choices: Map[Int, Boolean], facebookId: String) = {
    val chosenIndexes = choices.filter(_._2).map(_._1)
    val chosenPrograms = chosenIndexes.map(programs(_))
    println(chosenPrograms.map(_.roviId.is).toList)
    val showIds = chosenPrograms.map(_.yapShowId.is).filter(_.isDefined).flatten.toSet.toList
    if (showIds.size > 1) {
      Text("Programs with multiple existing show ids chosen. Not creating a new show, please merge the chosen " +
        "shows first. Show ids: " + showIds)
    } else if (showIds.size == 1) {
      val show = YapShow.findByResourceId(showIds(0)).get

      val result = associateWithExistingShow(facebookId, showIds(0))
      chosenPrograms.foreach { program =>
        showAndProgramLinker.linkShowAndProgram(show, program)
      }

      result
    } else {
      val (newShow, result) = createNewShow(chosenPrograms.head, facebookId)
      chosenPrograms.tail.foreach { program =>
        showAndProgramLinker.linkShowAndProgram(newShow, program)
      }

      result
    }
  }

  private def jsResult(newResultContent: NodeSeq): JsCmd = JqJsCmds.JqSetHtml("result", newResultContent)

  private def messageShowResult(message: String, show: YapShow) = {
    <div>
      <span>{message}. Admin links:</span>
      {adminLinksForShow(show)}
    </div>
  }

  private def adminLinksForShow(yapShow: YapShow): NodeSeq =  {
    val yapShowPresenter = new YapShowPresenter(yapShow)

    <ul>
      <li>{yapShowPresenter.presentOldAdminLink}</li>
      <li>{yapShowPresenter.presentFancyAdminLink}</li>
    </ul>
  }
}

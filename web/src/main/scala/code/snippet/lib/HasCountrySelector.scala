package code.snippet.lib

import tv.yap.model.YapCountry
import net.liftweb.http.{S, SHtml}
import net.liftweb.common.Empty
import com.foursquare.rogue.Rogue._
import net.liftweb.http.js.JsCmd

trait HasCountrySelector {
  val countries = YapCountry where (_.hasEpg eqs(true)) orderAsc (_.name) fetch()

  def handleCountrySelected(chosen:Option[YapCountry]):JsCmd

  /**
   * Build a country selector, this can be accessed directly from the snippet.
   *
   * @return
   */
  def countriesAjaxSelect = {
    val default = S.attr("code").flatMap(in=>{
      println("Looking for country with code " + in)
      countries.filter(_.code.is == in).headOption.map(_.id.is.toString)})
    val select = SHtml.ajaxSelect(
      ("-1","-all-") :: countries.map{c=>(c.id.is.toString,c.name.is)},
      default,
      selected => {
        val selectedId = selected.toInt
        handleCountrySelected(countries.find(_.id.is == selectedId))
      }
    )
    select
  }

}


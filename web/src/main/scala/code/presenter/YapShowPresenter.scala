package code.presenter

import tv.yap.model.messaging.YapShow
import xml._

object YapShowPresenter extends StyleablePresenter {

  implicit def listOfShowsPresenter(l: List[YapShow]) = new YapShowsPresenter(l)

  implicit def showPresenter(s: YapShow) = new YapShowPresenter(s)
}

class YapShowsPresenter(l: List[YapShow]) {

  import YapShowPresenter._

  def presentAsUl: NodeSeq =
    <ul>
      { l.map( show => <li>{show.name.is}</li> ) }
    </ul>

  def presentAsUl(fields: Function1[YapShow, Any]*): NodeSeq =
    <ul>
      { l.map( show => <li>{ show.present(fields: _*) }</li>) }
    </ul>
}

class YapShowPresenter(show: YapShow) {

  def present(fields: Function1[YapShow, Any]*): NodeSeq =
    fields.map { field => <span>{ field(show) }, </span> } // todo separator should be configurable

  def presentOldAdminLink: NodeSeq =
    <a>Old admin</a> % Attribute(None, "href", Text("http://admin.yap.tv/admin/yap_shows/" + show.id.is +"/edit"), Null)

  def presentFancyAdminLink: NodeSeq =
    <a>Fancy admin</a> % Attribute(None, "href", Text("http://admin.yap.tv/shows/" + show.id.is +"#social"), Null)

  def presentAssetsLink: NodeSeq =
    <a>Assets</a> % Attribute(None, "href", Text("http://admin.yap.tv/lifted/shows/assets?yap_show_id=" + show.resourceId.is), Null)
}

package code.lib

import tv.yap.model.messaging.YapShow

object YapUrlUtil {
  def showEdit(show:YapShow)="http://admin.yap.tv/admin/yap_shows/"+show.id.is+"/edit"
  def fancyEdit(show:YapShow)="http://admin.yap.tv/shows/"+show.id.is+"#social"
  def showView(show:YapShow)="http://www.yap.tv/show/" + show.resourceId.is
}

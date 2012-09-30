package code.snippet

import java.util.Date
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import Helpers._
import com.foursquare.rogue.Rogue._
import tv.yap.model.YapShowGroup
import net.liftweb.http.S

class YapShowGroups {
  def list = {
    val all = YapShowGroup fetch()
    ".show_group_row" #> all.map{group=>
      ".name *+" #> group.name.is &
      ".show_count *+" #> group.yapShowIds.is.length  &
      "a [href]" #> ("/show-groups/edit/" + group.id.is.toString)
    }
  }

  def syncToFeeds = {
    val all = YapShowGroup fetch() filter (_.feedContainersFetched.length>0)
    ".show_group_row" #> all.map(group=>
      ".name *+" #> group.name &
      ".containers *+" #> group.feedContainersFetched.map(_.id).mkString(",") &
      ".is_synced *+" #> group.isSynced
    )
  }

  def show = {
    val group = YapShowGroup.find(S.param("id").get).getOrElse(throw new Exception("Not found"))
    "#name" #> group.name &
    "#containers" #> group.feedContainersFetched.map(_.id).mkString(", ") &
    "#is-synced" #> group.isSynced
  }
}

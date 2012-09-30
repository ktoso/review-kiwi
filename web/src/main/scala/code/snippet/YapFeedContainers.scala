package code.snippet

import java.util.Date
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import Helpers._
import com.foursquare.rogue.Rogue._
import tv.yap.model.showfeed.YapShowFeedItemContainer

class YapFeedContainers {
  def list = {
    val all = YapShowFeedItemContainer orderAsc(_.facebookIds) fetch()
    ".feed_container_row" #> all.map{fc=>
      ".facebook_id *+" #> fc.facebookIds.is.mkString(", ") &
      ".show_ids *+" #> fc.yapShowIds.is.mkString(", ")
    }
  }
}

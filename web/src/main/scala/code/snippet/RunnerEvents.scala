package code.snippet

import net.liftweb.util._
import Helpers._
import com.foursquare.rogue.Rogue._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeZone, DateTime}
import tv.yap.model.operations.RunnerEvent

class RunnerEvents {

  val SimpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC)

  def list = {
    val all = RunnerEvent orderDesc(_.createdAt) limit(100) fetch()

    ".runner-events" #> all.map { event =>
      ".runner *+" #> event.runner.is &
      ".event *+" #> event.event.is &
      ".details *+" #> event.details.is &
      ".at *+" #> SimpleDateFormat.print(new DateTime(event.createdAt.is))
    }
  }
}

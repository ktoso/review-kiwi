package code.snippet.reports

import net.liftweb.util._
import Helpers._
import tv.yap.model._
import net.liftweb.http.{StatefulSnippet, S}
import net.liftweb.mongodb.MongoDB
import rovi.YapScheduleMetas
import tv.yap.mongo.MongoConfig.RoviEpgMongoIdentifier
import com.weiglewilczek.slf4s.Logging

class ScheduleCollections extends StatefulSnippet with Logging {

  override def dispatch = {
    case _ => list
  }

  val includeDaily = false

  lazy val yapCountries = YapCountry.findAll

  def list = {
    val all = YapScheduleMetas.all(includingDaily = includeDaily)
    ".collection_row" #> all.map { col =>
      ".name *+" #> col.collectionName &
      ".country *+" #> countryNameFor(col.collectionName) &
      ".movies_count *+" #> countMoviesOn(col) &
      ".total_count *+" #> col.count
    }
  }

  def countryNameFor(collectionName: String): String = {
    val flavor = YapScheduleFlavor.fromCollectionName(collectionName)
    flavor.flatMap(_.countryId) match {
      case Some(countryId) => yapCountries.find(_.id.is == countryId).get.name.is
      case _ => ""
    }
  }

  def countMoviesOn(meta: YapScheduleMeta): Long = {
    import com.foursquare.rogue.Rogue._

    val query = meta where (_.programType eqs "Movie") setSlaveOk (true)

    meta count (query.asDBObject)
  }
}

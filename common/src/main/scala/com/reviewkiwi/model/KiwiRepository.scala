package com.reviewkiwi.model

import mongo.MongoConfig.KiwiMongoIdentifier
import net.liftweb.record.field._
import scala.Some
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import com.foursquare.rogue.Rogue._
import java.util.Locale
import net.liftweb.record.UnderscoreName
import net.liftweb.common.Empty
import org.bson.types.ObjectId

class KiwiRepository private() extends MongoRecord[KiwiRepository] with ObjectIdPk[KiwiRepository] {

  lazy val meta = KiwiRepository

  object githubRepoId extends OptionalLongField(this) with UnderscoreName

  object fetchUrl extends StringField(this, 1000) with UnderscoreName {
    override def apply(in: MyType) = {
      super.apply(in)
    }
  }

  object name extends StringField(this, 255) with UnderscoreName

  /** Set to true if any user is interested in the contents of this repo */
  object fetchUsingPooling extends BooleanField(this, false) with UnderscoreName

  object watchers  extends MongoListField[KiwiRepository, ObjectId](this)   with UnderscoreName

}

object KiwiRepository extends KiwiRepository with MongoMetaRecord[KiwiRepository] {
  override def mongoIdentifier = KiwiMongoIdentifier
  override def collectionName = "repositories"

  def ensureIndexes() {
    ensureIndex((fetchUrl.name -> 1), ("unique" -> true))
    ensureIndex((githubRepoId.name -> 1), ("unique" -> true))
    ensureIndex(watchers.name -> 1)
    ensureIndex(name.name -> 1)
  }

  // finders

  import com.foursquare.rogue.Rogue._

  def findAllToFetch(): List[KiwiRepository] =
    meta where(_.fetchUsingPooling eqs true) fetch()

}

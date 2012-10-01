package com.reviewkiwi.model

import net.liftweb.record.field._
import scala.Some
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import com.foursquare.rogue.Rogue._
import java.util.Locale
import net.liftweb.record.UnderscoreName
import net.liftweb.common.Empty

class Repository private() extends MongoRecord[Repository] with ObjectIdPk[Repository] {

  lazy val meta = Repository

  object githubRepoId extends OptionalLongField(this) with UnderscoreName

  object fetchUrl extends StringField(this, 1000) with UnderscoreName {
    override def apply(in: MyType) = {
      super.apply(in)
    }
  }

  object name extends StringField(this, 255) with UnderscoreName

  object commiters extends MongoListField[Repository, Int](this)   with UnderscoreName
  object watchers  extends MongoListField[Repository, Int](this)   with UnderscoreName

}

object Repository extends Repository with MongoMetaRecord[Repository] {
//  override def mongoIdentifier = UserShowMongoIdentifier
//  override def collectionName = "repositories"

}

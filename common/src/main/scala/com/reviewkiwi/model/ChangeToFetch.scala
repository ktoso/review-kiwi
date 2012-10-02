package com.reviewkiwi.model

import mongo.MongoConfig.KiwiMongoIdentifier
import net.liftweb.record.field._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.UnderscoreName

class ChangeToFetch private() extends MongoRecord[ChangeToFetch] with ObjectIdPk[ChangeToFetch] {

  lazy val meta = ChangeToFetch

  object objectId   extends StringField(this, 40) with UnderscoreName

  object repo       extends ObjectIdRefField(this, KiwiRepository) with UnderscoreName

  object pushAuthor extends OptionalStringField(this, 255) with UnderscoreName

  object createdAt  extends DateTimeField(this) with UnderscoreName
}

object ChangeToFetch extends ChangeToFetch with MongoMetaRecord[ChangeToFetch] {
  override def mongoIdentifier = KiwiMongoIdentifier
  override def collectionName = "changes_to_fetch"

  def ensureIndexes() {
    ensureIndex(objectId.name -> 1)
    ensureIndex(repo.name -> 1)
  }

}

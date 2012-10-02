package com.reviewkiwi.model

import mongo.MongoConfig.KiwiMongoIdentifier
import net.liftweb.record.field._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.UnderscoreName

class ChangeFetched private() extends MongoRecord[ChangeFetched] with ObjectIdPk[ChangeFetched] {

  lazy val meta = ChangeFetched

  object objectId  extends StringField(this, 40) with UnderscoreName

  object repo      extends ObjectIdRefField(this, KiwiRepository) with UnderscoreName

  object fetchedAt extends DateTimeField(this) with UnderscoreName
}

object ChangeFetched extends ChangeFetched with MongoMetaRecord[ChangeFetched] {
  override def mongoIdentifier = KiwiMongoIdentifier
  override def collectionName = "changes_fetched"

  def ensureIndexes() {
    ensureIndex(objectId.name -> 1)
    ensureIndex(repo.name -> 1)
  }

}

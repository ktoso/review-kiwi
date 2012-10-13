package com.reviewkiwi.model

import mongo.MongoConfig.KiwiMongoIdentifier
import net.liftweb.record.field._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.json.JsonDSL._
import net.liftweb.record.UnderscoreName

class KiwiUser private() extends MongoRecord[KiwiUser] with ObjectIdPk[KiwiUser] {

  lazy val meta = KiwiUser

  object name extends StringField(this, 255) with UnderscoreName
  object email extends StringField(this, 255) with UnderscoreName

  object oauthToken extends StringField(this, 500) with UnderscoreName
  object oauthTokenType extends StringField(this, 20) with UnderscoreName

  object repos extends MongoListField[KiwiUser, Long](this) with UnderscoreName

  object watchedRepos extends MongoListField[KiwiUser, Long](this) with UnderscoreName

}

object KiwiUser extends KiwiUser with MongoMetaRecord[KiwiUser] {
    override def mongoIdentifier = KiwiMongoIdentifier
    override def collectionName = "users"

  def ensureIndexes() {
    ensureIndex(watchedRepos.name -> 1)
    ensureIndex(oauthToken.name -> 1)
    ensureIndex(email.name -> 1)
  }

  // finders

  import com.foursquare.rogue.Rogue._

  def findOAuthToken(apiKey: String): Option[KiwiUser] = {
    meta where(_.oauthToken eqs apiKey) get()
  }
}

package com.reviewkiwi.model

import mongo.MongoConfig.KiwiMongoIdentifier
import net.liftweb.record.field._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.UnderscoreName
import org.eclipse.jgit.revwalk.RevCommit
import java.util.Calendar
import scalaz.Scalaz._

class ChangeFetched private() extends MongoRecord[ChangeFetched] with ObjectIdPk[ChangeFetched] {

  lazy val meta = ChangeFetched

  object repoName  extends StringField(this, 255) with UnderscoreName
  object objectId  extends StringField(this, 40) with UnderscoreName

  // todo use this
//  object repo      extends ObjectIdRefField(this, KiwiRepository) with UnderscoreName

  object fetchedAt            extends DateTimeField(this) with UnderscoreName
  object notifiedUsersAboutIt extends BooleanField(this, false) with UnderscoreName
}

object ChangeFetched extends ChangeFetched with MongoMetaRecord[ChangeFetched] {

  override def mongoIdentifier = KiwiMongoIdentifier
  override def collectionName = "changes_fetched"

  def ensureIndexes() {
    ensureIndex(objectId.name -> 1)
    ensureIndex(repoName.name -> 1)
  }

  // finders

  import com.foursquare.rogue.Rogue._

  // todo should use repo name
  def alreadyNotifiedAbout(repoName: String, commit: RevCommit): Boolean = {
    meta where(_.objectId eqs commit.getName) and(_.repoName eqs repoName) and(_.notifiedUsersAboutIt eqs true) exists()
  }

  // creation
  def createIfNotPersistedYet(repoName: String, commit: RevCommit): Option[ChangeFetched] = {
    val whereClause = meta where (_.repoName eqs repoName) and (_.objectId eqs commit.getName)

    ChangeFetched.where(_.notifiedUsersAboutIt exists false).modify(_.notifiedUsersAboutIt setTo true).updateMulti()

    if (whereClause exists())
      None
    else
      meta.createRecord
        .fetchedAt(Calendar.getInstance)
        .repoName(repoName)
        .notifiedUsersAboutIt(false)
        .objectId(commit.getName)
        .save(true)
        .some
  }

  // resolve
  def markAsAlreadyNotifiedAbout(repoName: String, commit: RevCommit): Option[ChangeFetched] = {
    meta where(_.repoName eqs repoName) and(_.objectId eqs commit.getName) findAndModify
      (_.objectId setTo commit.getName) and
      (_.repoName setTo repoName) and
      (_.notifiedUsersAboutIt setTo true) upsertOne(true)
  }

}

package com.reviewkiwi.model.mongo

import collection.JavaConversions
import com.mongodb.{Mongo, ServerAddress}
import net.liftweb.mongodb.{MongoIdentifier, MongoDB}
import com.weiglewilczek.slf4s.Logging
import com.reviewkiwi.model.mongo.MongoConfig.KiwiMongoIdentifier
import com.reviewkiwi.model.{KiwiUser, ChangeToFetch, ChangeFetched}

/**
 * Used to establish an MongoDB connection and also ensure all indexes are set.
 */
object MongoInit extends Logging {

  /**
   * Initializes Lift's Mongo-Record using the provided configuration, and the default mongo identifier.
   */
  def init(config: MongoConfig, verbose: Boolean = true) {
    defineDb(KiwiMongoIdentifier, config.mongoServers, config.mongoDatabase, verbose)
    
    ensureIndexes()
  }

  def defineDb(mongoIdentifier: MongoIdentifier, servers: String, dbName: String, printDatabases: Boolean = false) {
    val serverList = asServerAdresses(servers)

    if(printDatabases)
      logger.info("Defining [%s]: servers: %s, database name: [%s]".format(mongoIdentifier, serverList, dbName))

    MongoDB.defineDb(mongoIdentifier, createMongo(serverList), dbName)
  }

  def createMongo(serverList: List[ServerAddress]) = {
    // We need to use a different constructor if there's only 1 server to avoid startup exceptions where mongo thinks
    // it's in a replica set.
    if (serverList.size == 1) {
      new Mongo(serverList.head)
    } else {
      import JavaConversions._
      new Mongo(serverList)
    }
  }

  def ensureIndexes() {
    KiwiUser.ensureIndexes()
    KiwiUser.ensureIndexes()
    ChangeFetched.ensureIndexes()
    ChangeToFetch.ensureIndexes()
  }

  def asServerAdresses(servers: String): scala.List[ServerAddress] = {
    servers.split(",").map(new ServerAddress(_)).toList
  }
}

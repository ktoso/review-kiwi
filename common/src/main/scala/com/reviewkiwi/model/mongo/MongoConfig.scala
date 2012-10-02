package com.reviewkiwi.model.mongo

import com.weiglewilczek.slf4s.Logging
import java.util.Properties
import net.liftweb.mongodb.MongoIdentifier

trait MongoConfig {
  def mongoServers: String
  def mongoDatabase: String
}

object MongoConfig {
  case object KiwiMongoIdentifier extends MongoIdentifier {
    override val jndiName = "kiwi_mongo_db"
  }
}


trait Config extends MongoConfig

object Config extends Logging {

  def readFromProperties(): Config = {
    val props = new Properties()
    props.load(getClass.getResourceAsStream(ConfigResourceName))
    readFromProperties(props)
  }

  def readFromProperties(properties: Properties): Config = {
    new Config {
      val mongoServers = properties.getProperty("mongo.servers")
      val mongoDatabase = properties.getProperty("mongo.db")

      println("mongoServers = " + mongoServers)
      println("mongoDatabase = " + mongoDatabase)
    }
  }

  val ConfigResourceName = "/mongo.properties"
}

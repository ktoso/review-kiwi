package com.reviewkiwi.build

import sbt._
import Keys._

object DefaultBuildSettings {
  import Resolvers._

  val mongoDirectory = SettingKey[File]("mongo-directory")

  val defaultBuildSettings = Defaults.defaultSettings ++ Seq (
      organization  := "com.reviewkiwi",
      scalaVersion  := "2.9.1",
      resolvers     := kiwiResolvers,
      scalacOptions := Seq("-unchecked", "-deprecation"),
      parallelExecution := false, // We are starting mongo in tests.
      testOptions in Test <+= mongoDirectory map {
        md => Tests.Setup { () =>
          val mongoFile = new File(md.getAbsolutePath + "/bin/mongod")
          if(mongoFile.exists) {
            System.setProperty("mongo.directory", md.getAbsolutePath)
          } else {
            throw new RuntimeException("Unable to find [mongodb] in 'mongo.directory' (%s). Please check your ~/.sbt/local.sbt file.".format(mongoFile.getAbsolutePath))
          }
        }
      }
    )
}

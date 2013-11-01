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
      parallelExecution := false // We are starting mongo in tests.
    )
}

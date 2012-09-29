package com.reviewkiwi.build

import sbt._

object Resolvers {

  val kiwiResolvers = Seq(
    "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases",
    "Typesafe Repository (snapshots)" at "http://repo.typesafe.com/typesafe/snapshots/",
    "SoftwareMill" at "http://tools.softwaremill.pl/nexus/content/groups/smlcommon-repos",
    "Spray" at "http://repo.spray.cc/",
    "Couchbase" at "http://files.couchbase.com/maven2/"
  )

}
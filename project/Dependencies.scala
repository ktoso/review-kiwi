package com.reviewkiwi.build

import sbt._
import Keys._

object Dependencies {

  val liftVersion = "2.4"
  val mysqlConnector        = "mysql"                  %  "mysql-connector-java"  % "5.1.15"
  val rogue                 = "com.foursquare"         %% "rogue"                 % "1.1.8" intransitive()
  val liftMongoRecord       = "net.liftweb"            %% "lift-mongodb-record"   % liftVersion
  val liftSquerylRecord     = "net.liftweb"            %% "lift-squeryl-record"   % liftVersion
  val h2                    = "com.h2database"         %  "h2"                    % "1.3.164"
  val c3p0                  = "c3p0"                   %  "c3p0"                  % "0.9.1.2"
  val liftJson              = "net.liftweb"            %% "lift-json"             % liftVersion
  val casbah                = "com.mongodb.casbah"     %% "casbah"                % "2.1.5-1"
  val zeromq                = "org.zeromq"             %% "zeromq-scala-binding"  % "0.0.1-SNAPSHOT"
  val twitter4j             = "org.twitter4j"          %  "twitter4j-core"        % "2.2.5"
  val restfb                = "com.restfb"             %  "restfb"                % "1.6.9"
  val jGit                  = "org.eclipse.jgit"       %  "org.eclipse.jgit"      % "1.3.0.201202151440-r"
  val spyMemcached          = "spy"                    %  "spymemcached"          % "2.8.1"
  val scalamd               = "org.fusesource.scalamd" %  "scalamd"               % "1.5.2"
  val zooKeeper             = ("org.apache.zookeeper"  %  "zookeeper"             % "3.3.5")
    .exclude("com.sun.jdmk", "jmxtools")
    .exclude("com.sun.jmx", "jmxri")
    .exclude("log4j", "log4j")
    .exclude("apache-log4j", "log4j")

  val zooKeeperClient       = "com.twitter" % "zookeeper-client" % "2.0.0" intransitive()
  val configgy              = "net.lag"     % "configgy"         % "2.0.1"
  val mongo                 = "org.mongodb"            %  "mongo-java-driver"     % "2.7.3"

  // Akka - must be compatible with Spray
  val akkaVersion           = "2.0.3"
  val akkaActor             = "com.typesafe.akka" % "akka-actor"          % akkaVersion withSources()
  val akkaSlf4j             = "com.typesafe.akka" % "akka-slf4j"          % akkaVersion withSources()
  val akkaTestKit           = "com.typesafe.akka" % "akka-testkit"        % akkaVersion % "test"
  val akkaFull              = Seq(akkaActor, akkaSlf4j, akkaTestKit)

  // Spray
  val sprayCan              = "cc.spray"              %   "spray-can"             % "0.9.3"
  val sprayServer           = "cc.spray"              %   "spray-server"          % "0.9.0"
  val sprayClient           = "cc.spray"              %   "spray-client"          % "0.9.0"

  // Scalate - templates
  val scalateWikitext = "org.fusesource.scalate" % "scalate-wikitext" % "1.5.3"
  val scalatePage     = "org.fusesource.scalate" % "scalate-page"     % "1.5.3"
  val scalateTest     = "org.fusesource.scalate" % "scalate-test"     % "1.5.3" % "test"
  val scalate = Seq(scalateWikitext, scalatePage, scalateTest)

  // Utils
  val scalaToolsTime        = "org.scala-tools.time"  %%  "time"                  % "0.5" intransitive()
  val jodaTime              = "joda-time"             %   "joda-time"             % "2.1"
  val jodaTimeConvert       = "org.joda"              %   "joda-convert"          % "1.2"
  val scalaz                = "org.scalaz"            %% "scalaz-core"            % "6.0.4"
  val httpClient            = "org.apache.httpcomponents" % "httpclient"          % "4.2.1"

  val guava                 = "com.google.guava"      %   "guava"                 % "11.0.2"
  //val smlCommonUtil         = "pl.softwaremill.common" %  "softwaremill-util"     % "64-SNAPSHOT"
  val scarg                 = "de.downgra"            % "scarg-core_2.8.1"        % "1.0.0-SNAPSHOT" from "http://up.project13.pl/files/scarg-core_2.8.1-1.0.0-SNAPSHOT.jar" // todo remove me, hack hack...
  val scopt                 = "com.github.scopt"     %% "scopt"                   % "2.0.0"

  // Gravatar
  val gravatar              = "de.bripkens"           % "gravatar4java"           % "1.1"

  // Apache Commonds
  val apacheCommonsEmail    = "org.apache.commons"    % "commons-email"           % "1.2"
  val apacheCommonsNet      = "commons-net"           % "commons-net"             % "3.0.1"
  val apacheCommonsIo       = "commons-io"            % "commons-io"              % "2.2"

  // Amazon Web Services
  val amazonJavaSdk         = "com.amazonaws"         %   "aws-java-sdk"          % "1.3.19"

  // Logging
  val slf4s                 = "com.weiglewilczek.slf4s" %% "slf4s"                % "1.0.7"
  val logback               = "ch.qos.logback"        % "logback-classic"         % "1.0.0"
  val log4jOverSlf4j        = "org.slf4j"             % "log4j-over-slf4j"        % "1.6.1"
  val jclOverSlf4j          = "org.slf4j"             % "jcl-over-slf4j"          % "1.6.1"
  val julToSlf4jBridge      = "org.slf4j"             % "jul-to-slf4j"            % "1.6.1"

  val logging               = Seq(slf4s, logback, log4jOverSlf4j, jclOverSlf4j)

  // xml
  val scalaxbLib            = "org.scalaxb"           %% "scalaxb"                % "0.7.3"

  // colored output in terminal
  val scalaRainbow          = "pl.project13.scala"     % "rainbow_2.9.1"          % "0.1"

  val elasticmqCore         = "org.elasticmq"          %% "elasticmq-core"        % "0.5"
  val elasticmqSqs          = "org.elasticmq"          %% "elasticmq-rest-sqs"    % "0.5"

  // Testing
  val scalatest             = "org.scalatest"          %% "scalatest"             % "1.8.RC1"
  val mockito               = "org.mockito"            %  "mockito-core"          % "1.8.5"

  val testing               = Seq(scalatest % "test", mockito % "test")

  // Monitoring
  val newrelicApi = "com.newrelic" % "newrelic-api" % "2.7.0"
  val metricsCore = "com.yammer.metrics" % "metrics-core" % "2.1.2"
  val metricsGanglia = "com.yammer.metrics" % "metrics-ganglia" % "2.1.2"
  val metrics = Seq(metricsCore, metricsGanglia)
}


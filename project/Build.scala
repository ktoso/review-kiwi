import sbt._
import Keys._
import ProguardPlugin._
import com.github.siasia.WebPlugin._

import com.reviewkiwi.build._

object ReviewKiwiBuildSettings {

  // See https://github.com/siasia/xsbt-proguard-plugin
  val proguardSettings = ProguardPlugin.proguardSettings ++ Seq(
    proguardOptions ++= Seq("-include proguard.cfg")
  )

  /* Required for Akka Dataflows */
  val akkaContinuationsSettings = Seq(
    autoCompilerPlugins := true,
    libraryDependencies <+= scalaVersion { v => compilerPlugin("org.scala-lang.plugins" % "continuations" % v) },
    scalacOptions += "-P:continuations:enable"
  )

  val buildSettings = DefaultBuildSettings.defaultBuildSettings ++
    proguardSettings
}

object ReviewKiwiBuild extends Build {
  import Dependencies._
  import ReviewKiwiBuildSettings._
  
  lazy val root: Project = Project(
    "review-kiwi",
    file("."),
    settings = buildSettings
  ) aggregate(repoWorker, common)

  lazy val common: Project = Project(
    "common",
    file("common"),
    settings = buildSettings ++
      Seq(
        libraryDependencies ++=
          Seq(jGit, mongo, rogue, liftMongoRecord) ++
            Seq(gravatar, apacheCommonsEmail, scalaz, guava) ++
            scalate ++
            testing ++ logging
      )
  )

  lazy val repoWorker: Project = Project(
    "repo-worker",
    file("repo-worker"),
    settings = buildSettings ++ akkaContinuationsSettings ++
      Seq(
        libraryDependencies ++= Seq(liftMongoRecord, rogue) ++
          akkaFull ++
          testing ++
          Seq()
      ) ++
      Seq(
        mainClass in (Compile, packageBin) := Some("com.reviewkiwi.repoworker.RepoWorker")
      )
  ) dependsOn (common)


  lazy val lifted = Project(
    "web-kiwi",
    file("web"),
    settings = buildSettings ++ Seq(
      ivyXML :=
        <dependencies>
            <exclude org="commons-logging" artifact="commons-logging" />
        </dependencies>,
      libraryDependencies ++= {
        Seq(
          "net.liftweb" %% "lift-webkit"  % liftVersion % "compile->default",
          "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container",
          "javax.servlet" % "servlet-api" % "2.5" % "provided",
          akkaActor, akkaSlf4j, akkaTestKit,scalaz,
          mysqlConnector,rogue,liftMongoRecord,liftSquerylRecord,zooKeeperClient,mongo,logback
        )
      }
    ) ++ webSettings
  ) dependsOn(common)

}

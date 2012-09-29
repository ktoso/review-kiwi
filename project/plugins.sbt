// Until https://github.com/siasia/xsbt-proguard-plugin/issues/15 is resolved, using our patched version.
// GitHub fork with the changes: https://github.com/adamw/xsbt-proguard-plugin
libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-proguard-plugin" % (v+"-0.1.2-sml"))

resolvers ++= Seq(
  "SotwareMill Public Releases" at "http://tools.softwaremill.pl/nexus/content/repositories/releases/",
  "Sonatype Public" at "https://oss.sonatype.org/content/repositories/public")

addSbtPlugin("net.virtualvoid" % "sbt-dependency-graph" % "0.5.1")

addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "0.7.3")

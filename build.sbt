
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += Resolver.defaultLocal

// https://github.com/akka/akka/issues/31064#issuecomment-1060871255
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always

val AkkaVersion = "2.6.20"
val echopraxiaVersion = "2.2.2"
val echopraxiaPlusScalaVersion = "1.1.1"

lazy val scala213 = "2.13.8"
lazy val scala212 = "2.12.16"
lazy val supportedScalaVersions = List(scala212, scala213)

val NoPublish = Seq(
  //Compile / doc := false,
  publish / skip := true
)

// Ensure that there is one and only one logback-test.xml file visible in testing
// https://youtrack.jetbrains.com/issue/SCL-16316/Test-runners-calculate-incorrect-test-classpath-include-dependencies-test-classpaths
lazy val logging = (project in file("logging")).settings(NoPublish).settings(
  crossScalaVersions := supportedScalaVersions,
  //
  libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.11" % Test,
  libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.11" % Test,
  libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.2" % Test
) // XXX needs to be NoPublish

lazy val actor = (project in file("actor")).settings(
  crossScalaVersions := supportedScalaVersions,
  //
  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion,
  libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion,

  // different styles of logger
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
).dependsOn(logging % "test->test")

lazy val actorTyped = (project in file("actor-typed")).settings(
  crossScalaVersions := supportedScalaVersions,
  //
  libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion,

  libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,

  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test,
  libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
).dependsOn(actor, logging % "test->test")

lazy val stream = (project in file("akka-stream")).settings(
  crossScalaVersions := supportedScalaVersions,
  //
  libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion,
  //
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  //
  libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion % Test,
  libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion % Test,
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test,
  libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test
).dependsOn(actor, logging % "test->test")

lazy val root = (project in file(".")).settings(NoPublish)
  .settings(
    crossScalaVersions := Nil,
    name := "echopraxia-plusakka"
  ).aggregate(actor, actorTyped, stream)

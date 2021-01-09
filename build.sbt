name := "Dixa-grcp"

version := "0.1"

scalaVersion := "2.12.12"


val AkkaVersion = "2.6.10"
val AkkaHttpVersion = "10.2.2"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,

  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  // resolve conflicts
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http2-support" % AkkaHttpVersion,
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.15.2" % "test",
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % "test",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % "test",
  "org.scalamock" %% "scalamock" % "5.1.0" % "test"

)

enablePlugins(AkkaGrpcPlugin)

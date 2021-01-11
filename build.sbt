name := "prime_numbers"

version := "0.1"

scalaVersion := "2.12.12"

val AkkaVersion = "2.6.10"
val AkkaHttpVersion = "10.2.2"
val LogbackVersion = "1.2.3"
val ScalaLoggingVersion = "3.9.2"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  // resolve conflicts
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http2-support" % AkkaHttpVersion,

  // logging
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
)

val ScalaMockVersion = "5.1.0"
val ScalaTestVersion = "3.2.3"
val ScalaCheck = "1.15.2"
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
  "org.scalacheck" %% "scalacheck" % ScalaCheck % "test",
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % "test",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % "test",
  "org.scalamock" %% "scalamock" % ScalaMockVersion % "test"

)

enablePlugins(AkkaGrpcPlugin)
enablePlugins(JavaServerAppPackaging)

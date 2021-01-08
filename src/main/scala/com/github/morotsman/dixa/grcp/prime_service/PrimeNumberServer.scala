package com.github.morotsman.dixa.grcp.prime_service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.{Config, ConfigFactory}
import com.github.morotsman.dixa.grcp._

import scala.concurrent.{ExecutionContext, Future}

object PrimeNumberServer {

  private val config = ConfigFactory.load().getConfig("prime_generator")
  private val location = config.getString("location")
  private val port = config.getInt("port")

  val serverConfig: Config = ConfigFactory
    .parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())

  implicit val system: ActorSystem = ActorSystem("Prime_number_generator", serverConfig)
  implicit val ec: ExecutionContext = system.dispatcher

  def main(args: Array[String]): Unit = {
    start(location, port)
  }

  def start(location: String, port: Int)(implicit system: ActorSystem, ec: ExecutionContext): Future[Http.ServerBinding] = {
    val service = PrimesServiceHandler(new PrimeNumberServiceImpl())

    val binding = Http()
      .newServerAt(location, port)
      .bind(service)

    binding.foreach { binding => println(s"gRPC server bound to: ${binding.localAddress}") }

    binding
  }
}



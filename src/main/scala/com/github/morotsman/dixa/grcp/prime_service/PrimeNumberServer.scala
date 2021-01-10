package com.github.morotsman.dixa.grcp.prime_service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.{Config, ConfigFactory}
import com.github.morotsman.dixa.grcp._
import com.github.morotsman.dixa.grcp.proxy.ProxyServer.logger
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}

object PrimeNumberServer extends LazyLogging{

  private val config = ConfigFactory.load().getConfig("prime_generator")
  private val location = config.getString("location")
  private val port = config.getInt("port")
  private val shutdownTimeout = config.getInt("shutdown.timeout.millis")

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

    val binding = Http().newServerAt(location, port)
      .bind(service)
      // https://doc.akka.io/docs/akka-http/current/server-side/graceful-termination.html
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = shutdownTimeout.millis))

    binding.onComplete{
      case Success(binding) =>
        logger.info(s"Prime genrator server bound to: ${binding.localAddress}")
      case Failure(exception) =>
        logger.info(s"Could not start the prime generator service: ${exception.getMessage}")
        system.terminate()
    }

    binding
  }
}



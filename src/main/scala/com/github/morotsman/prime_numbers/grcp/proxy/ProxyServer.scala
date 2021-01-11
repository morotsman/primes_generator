package com.github.morotsman.prime_numbers.grcp.proxy

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ProxyServer extends LazyLogging {

  private implicit val system: ActorSystem = ActorSystem("ProxyService")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val config = ConfigFactory.load().getConfig("proxy_service")
  private val port = config.getInt("port")
  private val location = config.getString("location")
  private val shutdownTimeout = config.getInt("shutdown.timeout.millis")

  def main(args: Array[String]): Unit = {
    ProxyServer.start(location, port)
  }

  private def start(location: String, port: Int)(implicit system: ActorSystem, ec: ExecutionContextExecutor): Future[Http.ServerBinding] = {
    val client = PrimeClientFactory(system)
    val proxyService = new ProxyService(client)

    val binding = Http().newServerAt(location, port)
      .bind(proxyService.route)
      // // https://doc.akka.io/docs/akka-http/current/server-side/graceful-termination.html
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = shutdownTimeout.millis))

    binding.onComplete{
      case Success(binding) =>
        logger.info(s"Proxy server bound to: ${binding.localAddress}")
      case Failure(exception) =>
        logger.info(s"Could not start the proxy service: ${exception.getMessage}")
        system.terminate()
    }

    binding
  }
}


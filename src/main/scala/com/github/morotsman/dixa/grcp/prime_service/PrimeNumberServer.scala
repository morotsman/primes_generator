package com.github.morotsman.dixa.grcp.prime_service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import com.github.morotsman.dixa.grcp._

import scala.concurrent.{ ExecutionContext, Future }

object PrimeNumberServer {

  val serverConfig = ConfigFactory
    .parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Prime_number_generator", serverConfig)
    new PrimeNumberServer(system).run()
  }
}

class PrimeNumberServer(system: ActorSystem) {
  def run(): Future[Http.ServerBinding] = {
    // Akka boot up code
    implicit val sys: ActorSystem = system
    implicit val ec: ExecutionContext = sys.dispatcher

    // Create service handlers
    val service: HttpRequest => Future[HttpResponse] =
      PrimesServiceHandler(new PrimeNumberServiceImpl())

    // TODO get from properties
    val binding = Http()
      .newServerAt("127.0.0.1", 8080)
      .bind(service)

    // report successful binding
    binding.foreach { binding => println(s"gRPC server bound to: ${binding.localAddress}") }

    binding
  }
}

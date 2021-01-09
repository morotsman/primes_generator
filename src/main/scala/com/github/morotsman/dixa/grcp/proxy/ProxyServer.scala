package com.github.morotsman.dixa.grcp.proxy

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}


object ProxyServer {

  private implicit val system: ActorSystem = ActorSystem("ProxyService")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val config = ConfigFactory.load()
  private val port = config.getConfig("proxy_service").getInt("port")
  private val location = config.getConfig("proxy_service").getString("location")

  def main(args: Array[String]): Unit = {
    ProxyServer.start(location, port)
  }

  private def start(location: String, port: Int)(implicit system: ActorSystem, ec: ExecutionContextExecutor): Future[Http.ServerBinding] = {
    val client = PrimeClientFactory(system)
    val proxyService = new ProxyService(client)

    val binding = Http().newServerAt(location, port).bind(proxyService.route)

    binding.foreach { binding => println(s"Proxy server bound to: ${binding.localAddress}") }
    binding
  }
}


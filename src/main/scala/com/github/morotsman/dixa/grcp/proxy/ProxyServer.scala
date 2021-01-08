package com.github.morotsman.dixa.grcp.proxy

import akka.actor.ActorSystem
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn
import akka.NotUsed
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Source
import com.github.morotsman.dixa.grcp._
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor


object ProxyServer {

  private implicit val system: ActorSystem = ActorSystem("ProxyService")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val config = ConfigFactory.load()
  private val port = config.getConfig("proxy_service").getInt("port")
  private val location = config.getConfig("proxy_service").getString("location")

  def main(args: Array[String]): Unit = {
    ProxyServer.start(location, port)
  }

  private def start(location: String, port: Int)(implicit system: ActorSystem, ec: ExecutionContextExecutor): Unit = {
    val client: PrimesServiceClient = PrimeClientFactory(system)

    // TODO error handling, logging, metrics
    val proxyService = new ProxyService(client)

    val binding = Http().newServerAt(location, 8081).bind(proxyService.route)

    binding.foreach { binding => println(s"Proxy server bound to: ${binding.localAddress}") }
  }
}


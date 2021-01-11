package com.github.morotsman.prime_numbers.grcp.proxy

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import com.github.morotsman.prime_numbers.grcp.PrimesServiceClient
import com.typesafe.config.ConfigFactory

object PrimeClientFactory {

  private val config = ConfigFactory.load()
  private val primeGeneratorLocation = config.getConfig("proxy_service").getString("prime.generator.location")
  private val primeGeneratorPort = config.getConfig("proxy_service").getInt("prime.generator.port")

  def apply(implicit system: ActorSystem): PrimesServiceClient = {
    val clientSettings = GrpcClientSettings.connectToServiceAt(primeGeneratorLocation, primeGeneratorPort).withTls(false)
    PrimesServiceClient(clientSettings)
  }

}

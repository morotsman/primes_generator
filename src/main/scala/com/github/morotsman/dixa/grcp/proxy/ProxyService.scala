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

import scala.concurrent.ExecutionContextExecutor


object ProxyService {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("ProxyService")
    implicit val ec: ExecutionContextExecutor = system.dispatcher


    // TODO properties
    val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8080).withTls(false)
    val client: PrimesService = PrimesServiceClient(clientSettings)


    def primes(numberOfPrimes: Int): Source[PrimesReply, NotUsed] = client.generatePrimes(PrimesRequest(numberOfPrimes))


    // TODO error handling, test, logging, metrics

    val route =
      get {
        path("prime" / IntNumber) { numberOfPrimes => {
          complete {
            HttpEntity(
              ContentTypes.`text/plain(UTF-8)`,
              // transform each number to a chunk of bytes
              primes(numberOfPrimes).map(n => ByteString(s"${n.prime}\n"))
            )
          }
        }
        }
      }

    val bindingFuture = Http().newServerAt("localhost", 8081).bind(route)
    println(s"Server online at http://localhost:8081/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

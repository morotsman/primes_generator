package com.github.morotsman.dixa.grcp.proxy

import akka.util.ByteString
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.NotUsed
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import com.github.morotsman.dixa.grcp._

class ProxyService(client: PrimesService) {
  private def primes(numberOfPrimes: Int): Source[PrimesReply, NotUsed] = client.generatePrimes(PrimesRequest(numberOfPrimes))

  val route: Route =
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
}

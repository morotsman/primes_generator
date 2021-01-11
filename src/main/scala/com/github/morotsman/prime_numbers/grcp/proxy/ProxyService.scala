package com.github.morotsman.prime_numbers.grcp.proxy

import akka.util.ByteString
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.NotUsed
import akka.event.Logging
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import com.github.morotsman.prime_numbers.grcp._


class ProxyService(client: PrimesService) {

  private def primes(numberOfPrimes: Int): Source[PrimesReply, NotUsed] = client.generatePrimes(PrimesRequest(numberOfPrimes))

  val route: Route =
    get {
      path("prime" / IntNumber) { numberOfPrimes => {
        logRequest(s"prime/$numberOfPrimes", Logging.InfoLevel) {
          complete {
            val source = primes(numberOfPrimes).map(n => ByteString(s"${n.prime}\n"))
            // It's possible to add some error handling here: https://doc.akka.io/docs/akka/current/stream/stream-error.html
            HttpEntity(ContentTypes.`text/plain(UTF-8)`, source)
          }
        }
      }
      }
    }
}

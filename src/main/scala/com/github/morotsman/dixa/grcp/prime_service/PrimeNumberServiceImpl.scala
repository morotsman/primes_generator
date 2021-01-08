package com.github.morotsman.dixa.grcp.prime_service

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.github.morotsman.dixa.grcp._


// TODO is mat needed?
class PrimeNumberServiceImpl(implicit mat: Materializer) extends PrimesService {

  import PrimeNumberGenerator._

  override def generatePrimes(in: PrimesRequest): Source[PrimesReply, NotUsed] = {
    Source(primes).takeWhile(p => p <= in.upTo).map(p => PrimesReply(p))
  }

}

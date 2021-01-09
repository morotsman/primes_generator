package com.github.morotsman.dixa.grcp.prime_service

import akka.NotUsed
import akka.grpc.GrpcServiceException
import akka.stream.scaladsl.Source
import com.github.morotsman.dixa.grcp._
import io.grpc.Status

class PrimeNumberServiceImpl() extends PrimesService {

  import PrimeNumberGenerator._

  override def generatePrimes(in: PrimesRequest): Source[PrimesReply, NotUsed] = {
    if(in.upTo >= 0) {
      Source(primes).takeWhile(p => p <= in.upTo).map(p => PrimesReply(p))
    } else {
      val error = new GrpcServiceException(Status.INVALID_ARGUMENT.withDescription("upTo must be greater or equal to zero"))
      Source.failed(error)
    }
  }

}

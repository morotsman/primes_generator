package com.github.morotsman.dixa.grcp.functional_test.prime_service

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.http.scaladsl.Http
import akka.stream.scaladsl.Sink
import akka.testkit.TestKit
import com.github.morotsman.dixa.grcp.prime_service.PrimeNumberServer
import com.github.morotsman.dixa.grcp.{PrimesReply, PrimesRequest, PrimesServiceClient}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import io.grpc.Status

class PrimeNumberServiceImplTest extends TestKit(ActorSystem("PrimesServer", PrimeNumberServer.serverConfig))
  with AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures
{

  private val location = "localhost"
  private val port = 8080

  private implicit val clientSystem: ActorSystem = ActorSystem("PrimesClient")
  private val clientSettings = GrpcClientSettings.connectToServiceAt(location, port).withTls(false)
  private val client = PrimesServiceClient(clientSettings)

  override def beforeAll(): Unit = {
    val ec = system.dispatcher
    val bound: Future[Http.ServerBinding] = PrimeNumberServer.start(location, port)(system, ec)
    bound.futureValue
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
    clientSystem.terminate()
  }

  def requestUpTo(upTo: Int) : List[PrimesReply] = {
    val stream = client.generatePrimes(PrimesRequest(upTo))
    val test = stream.runWith(Sink.seq)
    Await.result(test, 3.seconds ).toList
  }

  "PrimeNumberService" should {

    "reply with INVALID_ARGUMENT if a negative upTo was requests" in {
      val thrown = intercept[Exception] {
        requestUpTo(-1)
      }
      val status = Status.fromThrowable(thrown)
      assert(status.getDescription === "upTo must be greater equal or equal to zero")
      assert(status.getCode === Status.INVALID_ARGUMENT.getCode)
    }

    "reply with 0 prime if upTo 0 was requested" in {
      val result = requestUpTo(1)
      result should ===(List())
    }

    "reply with one prime if upTo 2 was requested" in {
      val result = requestUpTo(2)
      result should ===(List(PrimesReply(2)))
    }

    "reply with 10 primes if upTo 29 was requested" in {
      val result = requestUpTo(29)
      result should ===(List(
        PrimesReply(2), PrimesReply(3), PrimesReply(5), PrimesReply(7), PrimesReply(11),
        PrimesReply(13), PrimesReply(17), PrimesReply(19), PrimesReply(23), PrimesReply(29)
      ))
    }

    "reply with 10 primes if upTo 30 was requested" in {
      val result = requestUpTo(30)
      result should ===(List(
        PrimesReply(2), PrimesReply(3), PrimesReply(5), PrimesReply(7), PrimesReply(11),
        PrimesReply(13), PrimesReply(17), PrimesReply(19), PrimesReply(23), PrimesReply(29)
      ))
    }
  }

}

package com.github.morotsman.prime_numbers.grcp.prime_service

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Sink
import com.github.morotsman.prime_numbers.grcp.{PrimesReply, PrimesRequest}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration._

class PrimeNumberServiceImplTest extends AnyWordSpec
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {

  private val testKit = ActorTestKit()

  private implicit val patience: PatienceConfig = PatienceConfig(scaled(5.seconds), scaled(100.millis))

  private implicit val system: ActorSystem[_] = testKit.system

  private val service = new PrimeNumberServiceImpl()

  override def afterAll: Unit = {
    testKit.shutdownTestKit()
  }

  def requestUpTo(upTo: Int): List[PrimesReply] = {
    val stream = service.generatePrimes(PrimesRequest(upTo))
    val result = stream.runWith(Sink.seq)
    Await.result(result, 3.seconds).toList
  }

  "PrimeNumberServiceImpl" should {

    "reply with INVALID_ARGUMENT if a negative upTo was requests" in {
      val thrown = intercept[Exception] {
        requestUpTo(-1)
      }
      assert(thrown.getMessage === "upTo must be greater or equal to zero")
    }

    "reply with a stream of 0 prime numbers if upTo 0 is requested" in {
      val result = requestUpTo(0)
      result should ===(List())
    }

    "reply with a stream of 1 prime number if upTo 2 is requested" in {
      val result = requestUpTo(2)
      result should ===(List(PrimesReply(2)))
    }

    "reply with a stream of 3 prime numbers if upTo 5 is requested" in {
      val result = requestUpTo(5)
      result should ===(List(PrimesReply(2), PrimesReply(3), PrimesReply(5)))
    }

    "reply with a stream of 3 prime numbers if upTo 6 is requested" in {
      val result = requestUpTo(6)
      result should ===(List(PrimesReply(2), PrimesReply(3), PrimesReply(5)))
    }
  }


}

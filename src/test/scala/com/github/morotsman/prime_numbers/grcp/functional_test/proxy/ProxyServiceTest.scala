package com.github.morotsman.prime_numbers.grcp.functional_test.proxy

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Source
import com.github.morotsman.prime_numbers.grcp.proxy.ProxyService
import com.github.morotsman.prime_numbers.grcp.{PrimesReply, PrimesRequest, PrimesService}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProxyServiceTest extends AnyWordSpec with Matchers with ScalatestRouteTest with MockFactory {

  // I will still use a scala-mock to simulate repsonses from the PrimeService
  // Another option would be to use some kind of gRcp mock server
  private val primeServiceMock = mock[PrimesService]

  private val route = new ProxyService(primeServiceMock).route

  "The service" should {
    "return prime numbes for request GET prime/17" in {
      val primes = Source(List(2, 3, 5, 7, 11, 13, 17).map(PrimesReply(_)))
      (primeServiceMock.generatePrimes _).expects(PrimesRequest(17)).returning(primes)

      Get("/prime/17") ~!> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual
          """2
            |3
            |5
            |7
            |11
            |13
            |17
            |""".stripMargin
      }
    }

    "return 404 for request /prime" in {
      Get("/prime") ~!> route ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[String] shouldEqual "The requested resource could not be found."
      }
    }

    "return PUT 405 for request /prime/17" in {
      Put("/prime/17") ~!> route ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }

    "return 404 path for request /prime/notAntInteger" in {
      Get("/prime/notAntInteger") ~!> route ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[String] shouldEqual "The requested resource could not be found."
      }
    }

    "return 404 path for request /prime/-17" in {
      Get("/prime/-17") ~!> route ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[String] shouldEqual "The requested resource could not be found."
      }
    }

  }

}

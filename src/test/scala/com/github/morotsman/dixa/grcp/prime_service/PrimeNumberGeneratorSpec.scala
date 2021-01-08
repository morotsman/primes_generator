package com.github.morotsman.dixa.grcp.prime_service

import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

class PrimeNumberGeneratorSpec extends Properties("PrimeNumberGenerator") {

  private val numberOfPrimes = Gen.choose(0, 42)

  private val knownPrimes = Set(
    2, 3, 5, 7, 11, 13, 17, 19, 23, 29,
    31, 37, 41, 43, 47, 53, 59, 61, 67, 71,
    73, 79, 83, 89, 97, 101, 103, 107, 109, 113,
    127, 131, 137, 139, 149, 151, 157, 163, 167, 173,
    179, 181)

  property("generate primes") = forAll(numberOfPrimes) { (numberOfPrimes: Int) =>
    val primeNumbers = PrimeNumberGenerator.primes.take(numberOfPrimes)
    primeNumbers.forall(p => knownPrimes.contains(p))
  }

}

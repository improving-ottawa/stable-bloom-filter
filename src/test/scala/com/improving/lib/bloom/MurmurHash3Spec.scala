package com.improving.lib.bloom

import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

object MurmurHash3Spec extends Properties("MurmurHash3") {

  import Gens._

  property("should have a low collision rate") =
    forAll(Gen.listOfN(10000, validInputString)) { inputs =>
      val hashes = inputs.map(MurmurHash3.hashString)
      val numComparisons = hashes.size * (hashes.size - 1) / 2
      val numCollisions = hashes.distinct.size - hashes.size
      val collisionRate = numCollisions.toDouble / numComparisons
      collisionRate < 0.0001
    }

  property("should never return a negative number") = {
    val batchSize = 10000
    val numBatches = 30
    var failingInputs: List[String] = Nil
    (0 until numBatches).forall { _ =>
      val inputStrings =
        Gen.listOfN(batchSize, validInputString).sample.getOrElse(List.empty)
      val result = inputStrings.forall { str =>
        val hash = MurmurHash3.hashString(str)
        if (hash >= 0L) true
        else {
          failingInputs = str :: failingInputs
          false
        }
      }
      if (!result) {
        println(s"Failed for the following sample input strings:")
        failingInputs.take(10).foreach(println)
      }
      failingInputs = Nil
      result
    }
  }
}

object Gens {
  private val numStrGen: Gen[String] =
    Gen.listOfN(16, Gen.numChar).map(_.mkString)
  private val yearGen: Gen[Int] = Gen.choose(2022, 2023)
  private val monthGen: Gen[Int] = Gen.choose(1, 12)
  private val dayGen = (year: Int, month: Int) =>
    Gen.choose(1, daysInMonth(year, month))
  private val dateGen = for {
    year <- yearGen
    month <- monthGen
    day <- dayGen(year, month)
  } yield f"$year%04d$month%02d$day%02d"

  private val uidGen = for {
    num <- numStrGen
    date <- dateGen
  } yield s"$num.$date"

  private val entityGen: String => Gen[String] = tag =>
    uidGen.map(uid => s"$tag|$uid")

  private val packageEntityGenerator: Gen[String] = entityGen("PackageEntity")

  private val tripEntityGenerator: Gen[String] = entityGen("TripEntity")

  private val containerEntityGenerator: Gen[String] = entityGen(
    "ContainerEntity"
  )

  private val tagGenerator: Gen[String] = for {
    timestamp <- Gen.choose(1640696400000L, 1740696400000L)
  } yield s"trip-event-7:$timestamp"

  val validInputString: Gen[String] = Gen
    .frequency(
      (4, packageEntityGenerator),
      (3, tripEntityGenerator),
      (2, containerEntityGenerator),
      (1, tagGenerator)
    )
    .retryUntil(_.nonEmpty)

  private def daysInMonth(year: Int, month: Int): Int =
    month match {
      case 2 =>
        if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
      case 4 | 6 | 9 | 11 => 30
      case _              => 31
    }
}

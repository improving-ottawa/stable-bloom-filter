package com.improving.lib.bloom

import bloomfilter.CanGenerateHashFrom
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StableBloomFilterSpec extends AnyWordSpec with Matchers {
  "StableBloomFilter" should {

    "return the optimal number of hash functions in the Stable Bloom Filter" in {

      StableBloomFilter.optimalNumberOfHashFunctions(falsePositiveRate =
        0.2
      ) shouldBe 2

      StableBloomFilter.optimalNumberOfHashFunctions(falsePositiveRate =
        0.1
      ) shouldBe 2

      StableBloomFilter.optimalNumberOfHashFunctions(falsePositiveRate =
        0.01
      ) shouldBe 4
    }

    "return the optimal number of decrement operations in the Stable Bloom Filter" in {
      StableBloomFilter.optimalStableNumberOfCellsToDecrement(
        optimalNumberOfHashFunctions = 3,
        cellSize = 1,
        falsePositiveRate = 0.01
      ) shouldBe 10

      StableBloomFilter.optimalStableNumberOfCellsToDecrement(
        optimalNumberOfHashFunctions = 6,
        cellSize = 4,
        falsePositiveRate = 0.01
      ) shouldBe 141
    }

    "calculate the number of bits required to store max value a cell is set to" in {
      StableBloomFilter.cellSize(1) shouldBe 1

      StableBloomFilter.cellSize(2) shouldBe 2

      StableBloomFilter.cellSize(3) shouldBe 2
    }

    "put and test the existence of items correctly" in {
      import CanGenerateHashFrom._
      val sbf = StableBloomFilter[String](
        numberOfCells = 1000,
        cellSize = 3,
        falsePositiveRate = 0.01
      )

      sbf.mightContain("new_item") shouldBe false

      val itemA = "item_a"
      sbf.put(itemA)
      sbf.mightContain(itemA) shouldBe true
    }

    "evict the items as they age" in {
      import CanGenerateHashFrom._
      val sbf = StableBloomFilter[String](
        numberOfCells = 1000,
        cellSize = 3,
        falsePositiveRate = 0.01
      )

      val itemA = "item_a"
      sbf.put(itemA)
      sbf.mightContain(itemA) shouldBe true

      (0 to 1000000).foreach { i =>
        val item = s"item_$i"
        sbf.put(item)
      }

      // `item_a` should have been evicted by now.
      sbf.mightContain(itemA) shouldBe false
    }
  }
}

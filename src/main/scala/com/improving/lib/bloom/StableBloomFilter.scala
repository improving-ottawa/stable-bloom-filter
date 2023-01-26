package com.improving.lib.bloom

import bloomfilter.CanGenerateHashFrom

import scala.util.Random

/** A Bloom filter is a space-efficient probabilistic data structure that is
  * used to test whether an element is a member of a set. False positive matches
  * are possible, but false negatives are not. In other words, a query returns
  * either “possibly in set” or “definitely not in set”. Elements can be added
  * to the set, but not removed.
  *
  * A Stable Bloom Filter (SBF) is designed to continuously evict stale
  * information in order to make room for more recently added elements. The
  * false-positive rate of a classic Bloom filter will eventually reach 1, at
  * which point all queries will result in a false positive. The stable-point
  * property of an SBF, however, means that the false-positive rate
  * asymptotically approaches a configurable fixed constant. Stable Bloom
  * Filters are particularly beneficial for cases where the size of the data set
  * is unknown and memory is limited. For example, an SBF can be used to
  * deduplicate events from an unbounded event stream with a specified upper
  * bound on false positives and minimal false negatives.
  *
  * see
  * [[https://webdocs.cs.ualberta.ca/~drafiei/papers/DupDet06Sigmod.pdf Stable Bloom Filter]]
  *
  * @param numberOfCells
  *   The number of cells in the stable bloom filter data structure.
  * @param cellSize
  *   The size of each cell
  * @param numHashFunctions
  *   Number of hash functions
  * @param numOfCellsToDecrement
  *   Number of cells to decrement
  * @tparam T
  *   Input and out element type
  */
class StableBloomFilter[T] private (
    numberOfCells: Int,
    cellSize: Int,
    numHashFunctions: Int,
    numOfCellsToDecrement: Int
)(implicit canGenerateHash: CanGenerateHashFrom[T]) {

  private var cells = new Cells(numberOfCells, cellSize)

  private val maxValue = cells.maxCellValue

  private def decrementCells(): Unit = {
    val r = Random.nextInt(numberOfCells)

    (0 until numOfCellsToDecrement).foreach { i =>
      val idx = (r + i) % numberOfCells
      cells = cells.decrease(idx, delta = 1)
    }
  }

  def mightContain(item: T): Boolean = {
    val (lowerHash, upperHash) = hash(item)
    (0 until numHashFunctions)
      .collectFirst {
        case i
            if cells.get(
              nextHash(lowerHash, upperHash, i) % numberOfCells
            ) == 0 =>
          false
      }
      .getOrElse(true)
  }

  def put(item: T): Unit = {
    decrementCells()

    val (lowerHash, upperHash) = hash(item)

    (0 until numHashFunctions).foreach(i =>
      cells =
        cells.set(nextHash(lowerHash, upperHash, i) % numberOfCells, maxValue)
    )
  }

  private def nextHash(lowerHash: Int, upperHash: Int, index: Int): Int =
    math.abs(lowerHash + index * upperHash)

  private def hash(item: T): (Int, Int) = {
    val hash64 = canGenerateHash.generateHash(item)
    val lowerHash = hash64.toInt
    val upperHash = (hash64 >>> 32).toInt
    (lowerHash, upperHash)
  }
}

object StableBloomFilter {

  /** @param numberOfCells
    *   The number of cells in the stable bloom filter data structure.
    * @param cellSize
    *   The size of each cell
    * @param falsePositiveRate
    *   The false positive rate in a Bloom filter is the probability that a
    *   query for an item not actually stored in the filter will return a
    *   positive result. This happens when the same hash value is generated for
    *   two different items, causing the filter to return a false positive
    *   result. The false positive rate of a Bloom filter is dependent on the
    *   number of hash functions used and the size of the filter.
    * @param T
    *   Input and out element type
    * @return
    *   [[StableBloomFilter]]
    */
  def apply[T: CanGenerateHashFrom](
      numberOfCells: Int,
      cellSize: Int,
      falsePositiveRate: Double
  ): StableBloomFilter[T] = {

    val optimalNumHashFunctions = optimalNumberOfHashFunctions(
      falsePositiveRate
    )

    val numHashFunctions: Int =
      if (optimalNumHashFunctions > numberOfCells) numberOfCells
      else if (optimalNumHashFunctions == 0) 1
      else optimalNumHashFunctions

    val numOfCellsToDecrement =
      optimalStableNumberOfCellsToDecrement(
        numHashFunctions,
        cellSize,
        falsePositiveRate
      )

    new StableBloomFilter(
      numberOfCells,
      cellSize,
      numHashFunctions,
      numOfCellsToDecrement
    )

  }

  private def log2(x: Double): Double = math.log(x) / math.log(2)

  private[bloom] def cellSize(maxValue: Int): Int =
    math.ceil(log2(maxValue + 1)).toInt

  /** @param falsePositiveRate
    *   Desired rate of false positives
    * @return
    *   The optimal number of hash functions to use for a Stable Bloom filter
    *   based on the desired rate of false positives
    */
  private[bloom] def optimalNumberOfHashFunctions(
      falsePositiveRate: Double
  ): Int =
    math.ceil(log2(1.0 / falsePositiveRate) / 2).toInt

  /** @param optimalNumberOfHashFunctions
    *   The optimal number of hash functions
    * @param cellSize
    *   The size of each cell
    * @param falsePositiveRate
    *   Desired rate of false positives
    * @return
    *   The optimal number of cells to decrement, per iteration for the provided
    *   parameters of an SBF.
    */
  private[bloom] def optimalStableNumberOfCellsToDecrement(
      optimalNumberOfHashFunctions: Int,
      cellSize: Int,
      falsePositiveRate: Double
  ): Int = {
    val max: Double = math.pow(2, cellSize.toDouble) - 1
    val subDenom: Double =
      math.pow(
        1.0 - math
          .pow(falsePositiveRate, 1.0 / optimalNumberOfHashFunctions.toDouble),
        1.0 / max
      )
    val denom: Double =
      (1.0 / subDenom - 1.0) * (1.0 / optimalNumberOfHashFunctions.toDouble)

    val p: Int = (1.0 / denom).toInt
    if (p == 0) 1 else p
  }
}

package com.improving.lib.bloom

import scala.annotation.tailrec

/** Computes the MurmurHash3 hash value for a given input string.
  *
  * MurmurHash3 is a non-cryptographic hash function that is designed to be fast
  * and produce good distribution of hash values for a wide range of input data.
  * The algorithm takes an input data block of any length and produces a 64-bit
  * hash value. The algorithm works by dividing the input data block into 64-bit
  * chunks and processing them in sequence. For each chunk, the algorithm
  * applies a series of bitwise operations to mix the input bits and distribute
  * them across the output bits. The mixing operations involve a prime constant
  * m and a rotation constant r, which are carefully chosen to produce good
  * avalanche behavior and minimize collisions.
  *
  * For a more complete description of the algorithm and its variants, see
  * https://en.wikipedia.org/wiki/MurmurHash.
  */
private[bloom] object MurmurHash3 {

  def hashString(input: String): Long = {
    val data = input.getBytes("UTF-8")
    val len = data.length
    val seed = 0L

    // Constants for 64-bit variant
    // This is a 64-bit prime number with good dispersion properties
    val m = 0xc6a4a7935bd1e995L
    val r = 47

    // initialize the hash value with the seed and the length of the input data
    var h = seed ^ (len * m)

    // compute the hash value for each block of 8 bytes in the input data
    val nblocks = len / 8
    var i = 0

    while (i < nblocks) {
      var k = getBlock(data, i)
      k *= m
      k ^= k >>> r
      k *= m
      h ^= k
      h *= m
      i += 1
    }

    // compute the hash value for the remaining bytes in the input data
    if (len % 8 != 0) {
      var k = 0L
      var i2 = nblocks * 8
      while (i2 < len) {
        k |= (data(i2) & 0xffL) << (8 * (i2 % 8))
        i2 += 1
      }
      k *= m
      k ^= k >>> r
      k *= m
      h ^= k
      h *= m
    }

    h ^= h >>> r
    h *= m
    h ^= h >>> r

    /** apply two's complement to ensure that the result is positive The ~
      * operator performs a bitwise negation of the value, and the + 1L
      * expression adds 1 to the result. The & 0x7fffffffffffffffL expression
      * ensures that the result is positive by masking the sign bit
      */

    (~h + 1L) & 0x7fffffffffffffffL
  }

  /** Retrieves a block of 8 bytes from an array of bytes and returns it as a
    * long value.
    *
    * @param data
    *   the input data array
    * @param i
    *   the index of the block to retrieve
    * @param acc
    *   the accumulator value for the block
    * @param shift
    *   the bit shift for the block
    * @return
    *   the block of 8 bytes as a long value
    */
  @tailrec
  private def getBlock(
      data: Array[Byte],
      i: Int,
      acc: Long = 0L,
      shift: Int = 0
  ): Long =
    if (shift == 64) acc
    else {
      val b = (data(i * 8 + shift / 8).toLong & 0xff) << shift % 8
      getBlock(data, i, acc | b, shift + 8)
    }
}

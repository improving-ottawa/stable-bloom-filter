package com.improving.lib.bloom

class Cells(val count: Int, val cellSize: Int) {
  require(cellSize <= 8, "max cellSize is 8")
  private val maxValue = (1 << cellSize) - 1
  private val data = Array.fill((count * cellSize + 7) / 8)(0.toByte)

  def maxCellValue: Int = maxValue

  def decrease(cell: Int, delta: Int): Cells = {
    val value = math.max(0, (getBits(cell * cellSize, cellSize) & 0xff) - delta)
    setBits(cell * cellSize, cellSize, value)
    this
  }

  def increment(cell: Int, delta: Int): Cells = {
    val value = (getBits(cell * cellSize, cellSize) & 0xff) + delta
    setBits(cell * cellSize, cellSize, value.min(maxValue))
    this
  }

  def set(cell: Int, value: Int): Cells = {
    setBits(cell * cellSize, cellSize, value.min(maxValue))
    this
  }

  def get(cell: Int): Int = getBits(cell * cellSize, cellSize) & 0xff

  private def getBits(offset: Int, length: Int): Int = {
    val byteIndex = offset / 8
    val byteOffset = offset % 8
    if (byteOffset + length > 8) {
      val rem = 8 - byteOffset
      getBits(offset, rem) | (getBits(offset + rem, length - rem) << rem)
    } else {
      val bitMask = (1 << length) - 1
      (data(byteIndex) & (bitMask << byteOffset)) >> byteOffset
    }
  }

  private def setBits(offset: Int, length: Int, bits: Int): Unit = {
    val byteIndex = offset / 8
    val byteOffset = offset % 8
    if (byteOffset + length > 8) {
      val rem = 8 - byteOffset
      setBits(offset, rem, bits)
      setBits(offset + rem, length - rem, bits >> rem)
    } else {
      val bitMask = (1 << length) - 1
      data(byteIndex) = (data(byteIndex) & ~(bitMask << byteOffset)).toByte
      data(byteIndex) =
        (data(byteIndex) | ((bits & bitMask) << byteOffset)).toByte
    }
  }
}

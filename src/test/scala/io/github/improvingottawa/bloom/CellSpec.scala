package io.github.improvingottawa.bloom

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CellSpec extends AnyWordSpec with Matchers {
  "Bucket" should {
    "calculate the max value a cell is set to, using the cell size" in {
      val cells = new Cells(count = 10, cellSize = 2)
      cells.maxCellValue shouldBe 3
    }

    "return the number of allocated cells" in {
      val cells = new Cells(count = 10, cellSize = 2)
      cells.count shouldBe 10
    }

    "ensure that the increment method increments the cells value by the correct delta" in {
      var cellsSize2 = new Cells(count = 5, cellSize = 2)

      cellsSize2 = cellsSize2.increment(cell = 0, delta = 1)
      cellsSize2.get(0) shouldBe 1

      // The value is clamped to zero and the maximum bucket value
      cellsSize2 = cellsSize2.decrease(cell = 1, delta = 1)
      cellsSize2.get(1) shouldBe 0

      cellsSize2 = cellsSize2.set(cell = 2, value = 100)
      cellsSize2.get(2) shouldBe 3

      cellsSize2 = cellsSize2.increment(cell = 3, delta = 2)
      cellsSize2.get(3) shouldBe 2

      var cellsSize3 = new Cells(count = 5, cellSize = 3)

      cellsSize3 = cellsSize3.increment(0, 1)
      cellsSize3.get(0) shouldBe 1

      cellsSize3 = cellsSize3.decrease(1, 1)
      cellsSize3.get(1) shouldBe 0

      cellsSize3 = cellsSize3.set(2, 100)
      cellsSize3.get(2) shouldBe 7

      cellsSize3 = cellsSize3.increment(3, 2)
      cellsSize3.get(3) shouldBe 2

      var cellsSize8 = new Cells(count = 5, cellSize = 8)

      cellsSize8 = cellsSize8.increment(0, 1)
      cellsSize8.get(0) shouldBe 1

      cellsSize8 = cellsSize8.decrease(1, 1)
      cellsSize8.get(1) shouldBe 0

      cellsSize8 = cellsSize8.set(2, 255)
      cellsSize8.get(2) shouldBe 255

      cellsSize8 = cellsSize8.increment(3, 2)
      cellsSize8.get(3) shouldBe 2

    }
  }
}

package snippets

import compiler.iface.Snapshot

class BoundaryBreakTests extends SnapshotTests:

  test("simple boundary break"):
    snapshot("boundary_simple") {
      @Snapshot
      object BoundaryExample:
        import scala.util.boundary, boundary.break

        def findFirst(xs: List[Int], pred: Int => Boolean): Option[Int] =
          boundary:
            for x <- xs do
              if pred(x) then break(Some(x))
            None

        def run: Option[Int] =
          findFirst(List(1, 2, 3, 4, 5), _ > 3)
    }

  test("boundary with label type"):
    snapshot("boundary_label") {
      @Snapshot
      object BoundaryLabel:
        import scala.util.boundary, boundary.break

        def sumUntilNegative(xs: List[Int]): Int =
          boundary[Int]:
            var sum = 0
            for x <- xs do
              if x < 0 then break(sum)
              sum += x
            sum

        def run: Int =
          sumUntilNegative(List(1, 2, 3, -1, 4, 5))
    }

  test("nested boundaries"):
    snapshot("boundary_nested") {
      @Snapshot
      object BoundaryNested:
        import scala.util.boundary, boundary.break

        def findInMatrix(matrix: List[List[Int]], target: Int): Option[(Int, Int)] =
          boundary:
            for (row, i) <- matrix.zipWithIndex do
              boundary:
                for (cell, j) <- row.zipWithIndex do
                  if cell == target then break(Some((i, j)))
            None

        def run: Option[(Int, Int)] =
          val matrix = List(
            List(1, 2, 3),
            List(4, 5, 6),
            List(7, 8, 9)
          )
          findInMatrix(matrix, 5)
    }

  test("boundary for validation"):
    snapshot("boundary_validation") {
      @Snapshot
      object BoundaryValidation:
        import scala.util.boundary, boundary.break

        def validatePositive(xs: List[Int]): Either[String, List[Int]] =
          boundary:
            for x <- xs do
              if x <= 0 then break(Left(s"Invalid: $x is not positive"))
            Right(xs)

        def run: String =
          val r1 = validatePositive(List(1, 2, 3))
          val r2 = validatePositive(List(1, -2, 3))
          s"$r1, $r2"
    }

  test("boundary with optional break"):
    snapshot("boundary_optional") {
      @Snapshot
      object BoundaryOptional:
        import scala.util.boundary, boundary.break

        def process(xs: List[Int]): Int =
          boundary:
            val sum = xs.sum
            if sum > 100 then break(100)
            if sum < 0 then break(0)
            sum

        def run: List[Int] =
          List(
            process(List(10, 20, 30)),
            process(List(50, 60, 70)),
            process(List(-5, -10, 2))
          )
    }

  test("boundary replacing return"):
    snapshot("boundary_return") {
      @Snapshot
      object BoundaryReturn:
        import scala.util.boundary, boundary.break

        def indexOf(xs: List[String], target: String): Int =
          boundary:
            for (x, i) <- xs.zipWithIndex do
              if x == target then break(i)
            -1

        def run: List[Int] =
          val list = List("a", "b", "c", "d")
          List(
            indexOf(list, "c"),
            indexOf(list, "x")
          )
    }

  test("boundary with helper function"):
    snapshot("boundary_helper") {
      @Snapshot
      object BoundaryHelper:
        import scala.util.boundary, boundary.break, boundary.Label

        def checkCondition(x: Int)(using Label[Option[String]]): Unit =
          if x < 0 then break(Some(s"negative: $x"))
          if x > 100 then break(Some(s"too large: $x"))

        def validate(xs: List[Int]): Option[String] =
          boundary:
            for x <- xs do checkCondition(x)
            None

        def run: String =
          val r1 = validate(List(1, 50, 99))
          val r2 = validate(List(1, -5, 99))
          val r3 = validate(List(1, 150, 99))
          s"$r1, $r2, $r3"
    }

  test("boundary vs exception performance pattern"):
    snapshot("boundary_pattern") {
      @Snapshot
      object BoundaryPattern:
        import scala.util.boundary, boundary.break

        def findPair(xs: List[Int], target: Int): Option[(Int, Int)] =
          boundary:
            for i <- xs.indices do
              for j <- (i + 1) until xs.length do
                if xs(i) + xs(j) == target then
                  break(Some((xs(i), xs(j))))
            None

        def run: Option[(Int, Int)] =
          findPair(List(1, 4, 7, 2, 9, 3), 11)
    }

end BoundaryBreakTests

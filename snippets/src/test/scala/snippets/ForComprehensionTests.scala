package snippets

import compiler.iface.Snapshot

class ForComprehensionTests extends SnapshotTests:

  test("simple for yield"):
    snapshot("for_simple") {
      @Snapshot
      def doubled(xs: List[Int]): List[Int] =
        for x <- xs yield x * 2

      @Snapshot
      def run: List[Int] =
        doubled(List(1, 2, 3))
    }

  test("for with filter"):
    snapshot("for_filter") {
      @Snapshot
      def evens(xs: List[Int]): List[Int] =
        for x <- xs if x % 2 == 0 yield x

      @Snapshot
      def run: List[Int] =
        evens(List(1, 2, 3, 4, 5, 6))
    }

  test("nested for"):
    snapshot("for_nested") {
      @Snapshot
      def pairs(xs: List[Int], ys: List[String]): List[(Int, String)] =
        for
          x <- xs
          y <- ys
        yield (x, y)

      @Snapshot
      def run: List[(Int, String)] =
        pairs(List(1, 2), List("a", "b"))
    }

  test("for with multiple generators and filters"):
    snapshot("for_complex") {
      @Snapshot
      def pythagorean(n: Int): List[(Int, Int, Int)] =
        for
          a <- (1 to n).toList
          b <- (a to n).toList
          c <- (b to n).toList
          if a * a + b * b == c * c
        yield (a, b, c)

      @Snapshot
      def run: List[(Int, Int, Int)] =
        pythagorean(15)
    }

  test("for with value definitions"):
    snapshot("for_definitions") {
      @Snapshot
      def process(xs: List[Int]): List[String] =
        for
          x <- xs
          doubled = x * 2
          squared = doubled * doubled
        yield s"$x -> $doubled -> $squared"

      @Snapshot
      def run: List[String] =
        process(List(1, 2, 3))
    }

  test("for with Option"):
    snapshot("for_option") {
      @Snapshot
      def combine(a: Option[Int], b: Option[Int]): Option[Int] =
        for
          x <- a
          y <- b
        yield x + y

      @Snapshot
      def run: String =
        val r1 = combine(Some(1), Some(2))
        val r2 = combine(Some(1), None)
        s"$r1, $r2"
    }

  test("for with Either"):
    snapshot("for_either") {
      @Snapshot
      def divide(a: Int, b: Int): Either[String, Int] =
        if b == 0 then Left("division by zero")
        else Right(a / b)

      @Snapshot
      def compute(a: Int, b: Int, c: Int): Either[String, Int] =
        for
          x <- divide(a, b)
          y <- divide(x, c)
        yield y

      @Snapshot
      def run: String =
        val r1 = compute(20, 2, 5)
        val r2 = compute(20, 0, 5)
        s"$r1, $r2"
    }

  test("imperative for"):
    snapshot("for_imperative") {
      @Snapshot
      def printAll(xs: List[Int]): List[String] =
        var results = List.empty[String]
        for x <- xs do
          results = results :+ s"Processing $x"
        results

      @Snapshot
      def run: List[String] =
        printAll(List(1, 2, 3))
    }

end ForComprehensionTests

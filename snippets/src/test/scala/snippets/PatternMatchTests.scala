package snippets

import compiler.iface.Snapshot

class PatternMatchTests extends SnapshotTests:

  test("simple pattern matching"):
    snapshot("pattern_simple") {
      @Snapshot
      def describe(x: Any): String =
        x match
          case 0 => "zero"
          case i: Int => s"integer: $i"
          case s: String => s"string: $s"
          case _ => "unknown"

      @Snapshot
      def run: String =
        describe(42) + ", " + describe("hello")
    }

  test("pattern matching with guards"):
    snapshot("pattern_guards") {
      @Snapshot
      def classify(n: Int): String =
        n match
          case x if x < 0 => "negative"
          case x if x == 0 => "zero"
          case x if x % 2 == 0 => "positive even"
          case _ => "positive odd"

      @Snapshot
      def run: String =
        List(-5, 0, 4, 7).map(classify).mkString(", ")
    }

  test("nested pattern matching"):
    snapshot("pattern_nested") {
      @Snapshot
      case class Address(city: String, zip: Int)

      @Snapshot
      case class Person(name: String, address: Address)

      @Snapshot
      def getCity(p: Person): String =
        p match
          case Person(_, Address(city, _)) => city

      @Snapshot
      def run: String =
        getCity(Person("Alice", Address("Boston", 12345)))
    }

  test("custom extractor object"):
    snapshot("pattern_extractor") {
      @Snapshot
      object Even:
        def unapply(n: Int): Option[Int] =
          if n % 2 == 0 then Some(n / 2) else None

      @Snapshot
      def describeNumber(n: Int): String =
        n match
          case Even(half) => s"$n is even, half is $half"
          case _ => s"$n is odd"

      @Snapshot
      def run: String =
        describeNumber(10) + "; " + describeNumber(7)
    }

  test("boolean extractor"):
    snapshot("pattern_boolean_extractor") {
      @Snapshot
      object Positive:
        def unapply(n: Int): Boolean = n > 0

      @Snapshot
      def check(n: Int): String =
        n match
          case Positive() => "positive"
          case _ => "not positive"

      @Snapshot
      def run: String =
        check(5) + ", " + check(-3)
    }

  test("sequence pattern matching"):
    snapshot("pattern_sequence") {
      @Snapshot
      def describeList(xs: List[Int]): String =
        xs match
          case Nil => "empty"
          case head :: Nil => s"single: $head"
          case head :: tail => s"head: $head, tail length: ${tail.length}"

      @Snapshot
      def run: String =
        List(
          describeList(Nil),
          describeList(List(1)),
          describeList(List(1, 2, 3))
        ).mkString("; ")
    }

  test("tuple pattern matching"):
    snapshot("pattern_tuple") {
      @Snapshot
      def processPair(pair: (Int, String)): String =
        pair match
          case (0, s) => s"zero with $s"
          case (n, "special") => s"$n is special"
          case (n, s) => s"$n and $s"

      @Snapshot
      def run: String =
        List(
          processPair((0, "hello")),
          processPair((42, "special")),
          processPair((7, "world"))
        ).mkString("; ")
    }

  test("or patterns"):
    snapshot("pattern_or") {
      @Snapshot
      def isWeekend(day: String): Boolean =
        day match
          case "Saturday" | "Sunday" => true
          case _ => false

      @Snapshot
      def run: String =
        List("Monday", "Saturday", "Sunday", "Friday")
          .map(d => s"$d: ${isWeekend(d)}")
          .mkString(", ")
    }

end PatternMatchTests

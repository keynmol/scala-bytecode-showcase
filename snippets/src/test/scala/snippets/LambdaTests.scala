package snippets

import compiler.iface.Snapshot

class LambdaTests extends SnapshotTests:

  test("simple lambda"):
    snapshot("lambda_simple") {
      @Snapshot
      def applyTwice(f: Int => Int, x: Int): Int =
        f(f(x))

      @Snapshot
      def run: Int =
        applyTwice(x => x + 1, 5)
    }

  test("lambda with multiple parameters"):
    snapshot("lambda_multi_param") {
      @Snapshot
      def combine(f: (Int, Int) => Int, a: Int, b: Int): Int =
        f(a, b)

      @Snapshot
      def run: Int =
        combine((x, y) => x * y + x + y, 3, 4)
    }

  test("lambda with placeholder syntax"):
    snapshot("lambda_placeholder") {
      @Snapshot
      def process(xs: List[Int]): List[Int] =
        xs.map(_ * 2).filter(_ > 5)

      @Snapshot
      def run: List[Int] =
        process(List(1, 2, 3, 4, 5))
    }

  test("closure capturing variable"):
    snapshot("lambda_closure") {
      @Snapshot
      def makeAdder(n: Int): Int => Int =
        x => x + n

      @Snapshot
      def run: Int =
        val add5 = makeAdder(5)
        val add10 = makeAdder(10)
        add5(3) + add10(3)
    }

  test("closure capturing mutable variable"):
    snapshot("lambda_mutable_closure") {
      @Snapshot
      def makeCounter(): () => Int =
        var count = 0
        () => {
          count += 1
          count
        }

      @Snapshot
      def run: List[Int] =
        val counter = makeCounter()
        List(counter(), counter(), counter())
    }

  test("eta expansion"):
    snapshot("lambda_eta") {
      @Snapshot
      def double(x: Int): Int = x * 2

      @Snapshot
      def applyToList(xs: List[Int], f: Int => Int): List[Int] =
        xs.map(f)

      @Snapshot
      def run: List[Int] =
        applyToList(List(1, 2, 3), double)
    }

  test("higher order function returning lambda"):
    snapshot("lambda_higher_order") {
      @Snapshot
      def compose[A, B, C](f: B => C, g: A => B): A => C =
        x => f(g(x))

      @Snapshot
      def run: Int =
        val addOne = (x: Int) => x + 1
        val double = (x: Int) => x * 2
        val addOneThenDouble = compose(double, addOne)
        addOneThenDouble(5)
    }

  test("partial application"):
    snapshot("lambda_partial") {
      @Snapshot
      def add(a: Int, b: Int, c: Int): Int =
        a + b + c

      @Snapshot
      def run: Int =
        val addFive = add(5, _, _)
        val addFiveAndTen = addFive(10, _)
        addFiveAndTen(3)
    }

  test("SAM conversion"):
    snapshot("lambda_sam") {
      @Snapshot
      trait Transformer:
        def transform(x: Int): Int

      @Snapshot
      def applyTransformer(t: Transformer, x: Int): Int =
        t.transform(x)

      @Snapshot
      def run: Int =
        applyTransformer(x => x * 3, 7)
    }

  test("nested lambdas"):
    snapshot("lambda_nested") {
      @Snapshot
      def curry(f: (Int, Int) => Int): Int => Int => Int =
        x => y => f(x, y)

      @Snapshot
      def run: Int =
        val curriedAdd = curry((a, b) => a + b)
        val add3 = curriedAdd(3)
        add3(7)
    }

end LambdaTests

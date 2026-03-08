package snippets

import compiler.iface.Snapshot

class MethodCallsTests extends SnapshotTests:

  test("simple method call"):
    snapshot("simple_call") {
      @Snapshot
      def greet(name: String): String =
        s"Hello, $name!"

      @Snapshot
      def run: String =
        greet("World")
    }

  test("multiple parameters"):
    snapshot("multiple_params") {
      @Snapshot
      def add(a: Int, b: Int, c: Int): Int =
        a + b + c

      @Snapshot
      def run: Int =
        add(1, 2, 3)
    }

  test("default parameters"):
    snapshot("default_params") {
      @Snapshot
      def configure(host: String, port: Int = 8080, secure: Boolean = false): String =
        val protocol = if secure then "https" else "http"
        s"$protocol://$host:$port"

      @Snapshot
      def run: String =
        val a = configure("localhost")
        val b = configure("example.com", 443, true)
        val c = configure("api.server", secure = true)
        s"$a, $b, $c"
    }

  test("multiple argument lists (curried)"):
    snapshot("curried") {
      @Snapshot
      def multiply(a: Int)(b: Int): Int =
        a * b

      @Snapshot
      def run: Int =
        val double = multiply(2)
        val triple = multiply(3)
        double(5) + triple(4)
    }

  test("by-name parameters"):
    snapshot("by_name") {
      @Snapshot
      def withLogging(label: String)(computation: => Int): Int =
        val result = computation
        result

      @Snapshot
      def run: Int =
        withLogging("expensive") {
          val x = 10
          val y = 20
          x * y
        }
    }
end MethodCallsTests

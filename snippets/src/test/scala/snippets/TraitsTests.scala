package snippets

import compiler.iface.Snapshot

class TraitsTests extends SnapshotTests:

  test("simple trait"):
    snapshot("trait_simple") {
      @Snapshot
      trait Greeter:
        def greet(name: String): String

      @Snapshot
      class FriendlyGreeter extends Greeter:
        def greet(name: String): String = s"Hello, $name!"

      @Snapshot
      def run: String =
        val g: Greeter = FriendlyGreeter()
        g.greet("World")
    }

  test("trait with default implementation"):
    snapshot("trait_default_impl") {
      @Snapshot
      trait Logger:
        def log(msg: String): String = s"[LOG] $msg"
        def warn(msg: String): String = s"[WARN] $msg"

      @Snapshot
      class AppLogger extends Logger

      @Snapshot
      def run: String =
        val logger = AppLogger()
        logger.log("Starting") + "; " + logger.warn("Low memory")
    }

  test("trait with abstract and concrete methods"):
    snapshot("trait_mixed_methods") {
      @Snapshot
      trait Formatter:
        def prefix: String
        def format(msg: String): String = s"$prefix: $msg"

      @Snapshot
      class ErrorFormatter extends Formatter:
        def prefix: String = "ERROR"

      @Snapshot
      def run: String =
        ErrorFormatter().format("Something went wrong")
    }

  test("multiple trait mixin"):
    snapshot("trait_mixin") {
      @Snapshot
      trait Swimmer:
        def swim: String = "swimming"

      @Snapshot
      trait Flyer:
        def fly: String = "flying"

      @Snapshot
      class Duck extends Swimmer with Flyer:
        def quack: String = "quack"

      @Snapshot
      def run: String =
        val duck = Duck()
        s"${duck.swim}, ${duck.fly}, ${duck.quack}"
    }

  test("trait linearization"):
    snapshot("trait_linearization") {
      @Snapshot
      trait Base:
        def value: String = "base"

      @Snapshot
      trait A extends Base:
        override def value: String = "A -> " + super.value

      @Snapshot
      trait B extends Base:
        override def value: String = "B -> " + super.value

      @Snapshot
      class C extends A with B

      @Snapshot
      def run: String =
        C().value
    }

  test("trait with fields"):
    snapshot("trait_fields") {
      @Snapshot
      trait Counter:
        var count: Int = 0
        def increment(): Unit = count += 1
        def current: Int = count

      @Snapshot
      class ClickCounter extends Counter

      @Snapshot
      def run: Int =
        val counter = ClickCounter()
        counter.increment()
        counter.increment()
        counter.current
    }

  test("self type"):
    snapshot("trait_self_type") {
      @Snapshot
      trait Persistence:
        def save(data: String): String = s"Saved: $data"

      @Snapshot
      trait UserService:
        this: Persistence =>
        def createUser(name: String): String =
          save(s"User($name)")

      @Snapshot
      class UserServiceImpl extends UserService with Persistence

      @Snapshot
      def run: String =
        UserServiceImpl().createUser("Alice")
    }

  test("sealed trait"):
    snapshot("trait_sealed") {
      @Snapshot
      sealed trait Result

      @Snapshot
      case class Success(value: Int) extends Result

      @Snapshot
      case class Failure(error: String) extends Result

      @Snapshot
      def describe(r: Result): String =
        r match
          case Success(v) => s"Success: $v"
          case Failure(e) => s"Failure: $e"

      @Snapshot
      def run: String =
        describe(Success(42)) + "; " + describe(Failure("oops"))
    }

end TraitsTests

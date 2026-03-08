package snippets

import compiler.iface.Snapshot

class EnumsTests extends SnapshotTests:

  test("simple enum"):
    snapshot("enum_simple") {
      @Snapshot
      enum Color:
        case Red, Green, Blue

      @Snapshot
      def describe(c: Color): String =
        c match
          case Color.Red => "hot"
          case Color.Green => "natural"
          case Color.Blue => "cool"

      @Snapshot
      def run: String =
        describe(Color.Red) + ", " + describe(Color.Blue)
    }

  test("enum with ordinal and values"):
    snapshot("enum_ordinal") {
      @Snapshot
      enum Priority:
        case Low, Medium, High

      @Snapshot
      def run: String =
        val all = Priority.values.map(p => s"${p.ordinal}:$p").mkString(", ")
        val fromName = Priority.valueOf("High")
        s"$all; valueOf: $fromName"
    }

  test("enum with parameters"):
    snapshot("enum_params") {
      @Snapshot
      enum Planet(val mass: Double, val radius: Double):
        case Mercury extends Planet(3.3e23, 2.4e6)
        case Earth extends Planet(6.0e24, 6.4e6)
        case Jupiter extends Planet(1.9e27, 7.1e7)

        def surfaceGravity: Double =
          val G = 6.67e-11
          G * mass / (radius * radius)

      @Snapshot
      def run: String =
        Planet.values.map(p => s"${p}: g=${p.surfaceGravity}").mkString("; ")
    }

  test("enum with methods"):
    snapshot("enum_methods") {
      @Snapshot
      enum Direction:
        case North, East, South, West

        def opposite: Direction = this match
          case North => South
          case South => North
          case East => West
          case West => East

        def turnRight: Direction = this match
          case North => East
          case East => South
          case South => West
          case West => North

      @Snapshot
      def run: String =
        val d = Direction.North
        s"$d opposite=${d.opposite} right=${d.turnRight}"
    }

  test("ADT enum"):
    snapshot("enum_adt") {
      @Snapshot
      enum Expr:
        case Num(value: Int)
        case Add(left: Expr, right: Expr)
        case Mul(left: Expr, right: Expr)

      @Snapshot
      def eval(e: Expr): Int =
        e match
          case Expr.Num(v) => v
          case Expr.Add(l, r) => eval(l) + eval(r)
          case Expr.Mul(l, r) => eval(l) * eval(r)

      @Snapshot
      def run: Int =
        import Expr.*
        eval(Add(Num(2), Mul(Num(3), Num(4))))
    }

  test("enum with type parameter"):
    snapshot("enum_generic") {
      @Snapshot
      enum Option[+T]:
        case Some(value: T)
        case None

      @Snapshot
      def getOrElse[T](opt: Option[T], default: T): T =
        opt match
          case Option.Some(v) => v
          case Option.None => default

      @Snapshot
      def run: String =
        val a = getOrElse(Option.Some("hello"), "default")
        val b = getOrElse(Option.None, "fallback")
        s"$a, $b"
    }

  test("enum with companion object"):
    snapshot("enum_companion") {
      @Snapshot
      enum Status:
        case Active, Inactive, Pending

      @Snapshot
      object Status:
        def fromString(s: String): Status =
          s.toLowerCase match
            case "active" | "on" => Active
            case "inactive" | "off" => Inactive
            case _ => Pending

      @Snapshot
      def run: String =
        List("active", "off", "unknown")
          .map(Status.fromString)
          .mkString(", ")
    }

  test("enum extending trait"):
    snapshot("enum_trait") {
      @Snapshot
      trait Describable:
        def description: String

      @Snapshot
      enum LogLevel extends Describable:
        case Debug, Info, Warn, Error

        def description: String = this match
          case Debug => "Detailed debugging information"
          case Info => "General information"
          case Warn => "Warning message"
          case Error => "Error condition"

      @Snapshot
      def run: String =
        LogLevel.Warn.description
    }

end EnumsTests

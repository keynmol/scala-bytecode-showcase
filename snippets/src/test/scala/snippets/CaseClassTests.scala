package snippets

import compiler.iface.Snapshot

class CaseClassTests extends SnapshotTests:

  test("simple case class"):
    snapshot("case_class_simple") {
      @Snapshot
      case class Point(x: Int, y: Int)

      @Snapshot
      def run: Point =
        Point(10, 20)
    }

  test("case class with default parameters"):
    snapshot("case_class_defaults") {
      @Snapshot
      case class Config(host: String, port: Int = 8080, secure: Boolean = false)

      @Snapshot
      def run: List[Config] =
        List(
          Config("localhost"),
          Config("example.com", 443, true),
          Config("api.server", secure = true)
        )
    }

  test("case class copy method"):
    snapshot("case_class_copy") {
      @Snapshot
      case class Person(name: String, age: Int)

      @Snapshot
      def run: Person =
        val original = Person("Alice", 30)
        val older = original.copy(age = 31)
        val renamed = original.copy(name = "Bob")
        renamed
    }

  test("case class pattern matching"):
    snapshot("case_class_pattern") {
      @Snapshot
      case class Rectangle(width: Int, height: Int)

      @Snapshot
      def area(shape: Rectangle): Int =
        shape match
          case Rectangle(w, h) => w * h

      @Snapshot
      def run: Int =
        area(Rectangle(5, 10))
    }

  test("nested case classes"):
    snapshot("case_class_nested") {
      @Snapshot
      case class Address(street: String, city: String)

      @Snapshot
      case class Employee(name: String, address: Address)

      @Snapshot
      def run: Employee =
        Employee("John", Address("123 Main St", "Springfield"))
    }

end CaseClassTests

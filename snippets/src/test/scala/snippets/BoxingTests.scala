package snippets

import compiler.iface.Snapshot

class BoxingTests extends SnapshotTests:

  test("boxing function call"):
    snapshot("boxing_function_call"):
      @Snapshot
      def sumSpecialised(f: Array[Float]): Float =
        var sum = 0f
        for s <- f do sum = s
        sum
      end sumSpecialised

      @Snapshot
      def sumSpecialisedDumb(f: Array[Float]): Float =
        var sum = 0f
        for s <- f do sum = sum + s
        sum
      end sumSpecialisedDumb

      @Snapshot
      def sumSpecialisedSuperDumb(f: Array[Float]): Float =
        var sum = 0f
        var i = 0
        while i < f.length do
          sum += f(i)
          i += 1
        sum
      end sumSpecialisedSuperDumb

      @Snapshot
      def sumGeneric[T](f: Array[T])(using n: Numeric[T]): T =
        var sum = n.zero
        for s <- f do sum = n.plus(sum, s)
        sum
      end sumGeneric

      @Snapshot
      def run(): Unit =
        val arr = Array[Float](1, 2, 3, 4)

        sumSpecialised(arr)
        sumGeneric(arr)
      end run

end BoxingTests

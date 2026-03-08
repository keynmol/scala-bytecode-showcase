# Scala 3 bytecode showcase

This project demonstrates the differences in bytecode encoding of Scala 3 code
between different Scala 3 versions.

The features covered can be found in the [snippets module](./snippets/src/test/scala/snippets), and the decompiled versions (both with [Vineflower](https://github.com/Vineflower/vineflower) and `javap`) are in version-specific folders in [snapshots folder](./snapshots).

We also provide a diff between the versions – e.g. [between 3.3.7 and 3.8.2](./snapshots/3.3.7_3.8.2_decompiled.diff).

The purpose is to see the Scala 3 encoding evolution and to be able to directly reference how various constructs are encoded in particular Scala version.

For example, here's how a [simple for comprehension is encoded in 3.8.2](https://github.com/keynmol/scala-bytecode-showcase/blob/main/snapshots/3.8.2/for_definitions_decompiled), and here's how [it was dramatically simplified compared to 3.3.7](https://github.com/keynmol/scala-bytecode-showcase/blob/main/snapshots/3.3.7_3.8.2_decompiled.diff#L980-L1005) thanks [Better fors SIP](https://www.scala-lang.org/api/current/docs/other-new-features/better-fors.html).

## Development

This is an sbt project.

Run `sbt projects` to see what projects are available.
For example, to rebuild the snapshots for 3.3.7 you can run `snippets3_3_7/test` – you can also build individual specs by using `snippets3_3_7/testOnly`.

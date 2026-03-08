package compiler.iface;

public interface CompilerInterface {
    CompilerInterface withClasspath(String[] cp);
    CompilationResult compile(String fileName, String contents, String outDir);
}

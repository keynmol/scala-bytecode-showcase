package compiler.iface;

public interface CompilationResult {
    boolean success();
    CompilationError[] errors();
    String[] classFiles();
}

package compiler.iface;

public interface CompilationError {
    int line();
    int column();
    String message();
}

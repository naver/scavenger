package io.codekvast.javaagent.codebase.scannertest;

/**
 * @author olle.hallin@crisp.se
 */
@SuppressWarnings({"UnusedDeclaration", "EmptyMethod"})
public class ScannerTest2 extends ScannerTest1 {
    public void m2() {
    }

    @Override
    public String defaultMethod_m2() {
        return "defaultMethod_m2 override";
    }
}

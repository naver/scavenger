package io.codekvast.javaagent.codebase.scannertest;

/**
 * @author olle.hallin@crisp.se
 */
@SuppressWarnings({"UnusedDeclaration", "UseOfSystemOutOrSystemErr"})
public class ScannerTest4 extends java.util.Date {
    public ScannerTest4(long date) {
        super(date);
    }

    @Override
    public long getTime() {
        return super.getTime() - 1;
    }

    public void m4(int i) {
        System.out.print("m4(int)");
    }

    public void m4(long l) {
        System.out.print("m4(long)");
    }

    public void m4(String s) {
        System.out.print("m4(String)");
    }

    private void m5(String s) {
        System.out.print("m5(String)");
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "ScannerTest4";
    }

    @Override
    public ScannerTest4 clone() {
        return (ScannerTest4)super.clone();
    }

    public static class StaticInner {
        public void m6(String s) {
            System.out.print("m6(String)");
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class Inner {
        public void m5(String s) {
            System.out.print("m5(String)");
        }
    }
}

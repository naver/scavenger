package io.codekvast.javaagent.util;

import static io.codekvast.javaagent.util.SignatureUtils.makeConstructorSignature;
import static io.codekvast.javaagent.util.SignatureUtils.makeLocation;
import static io.codekvast.javaagent.util.SignatureUtils.makeMethodLocation;
import static io.codekvast.javaagent.util.SignatureUtils.makeMethodSignature;
import static io.codekvast.javaagent.util.SignatureUtils.makeSignature;
import static io.codekvast.javaagent.util.SignatureUtils.signatureToString;
import static io.codekvast.javaagent.util.SignatureUtils.stripModifiers;
import static io.codekvast.javaagent.util.SignatureUtils.stripModifiersAndReturnType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.Test;

import io.codekvast.javaagent.config.MethodAnalyzer;
import io.codekvast.javaagent.model.v4.MethodLocation4;
import io.codekvast.javaagent.model.v4.MethodSignature4;
import lombok.val;

@SuppressWarnings("ALL")
public class SignatureUtilsTest {

    private static final String IDEA_TEST_CLASSES_PATH = "classes/";
    private static final String GRADLE_TEST_CLASSES_PATH = "build/classes/java/test/";
    private static final String LOCATION_PATTERN =
        String.format("(%s|%s)", IDEA_TEST_CLASSES_PATH, GRADLE_TEST_CLASSES_PATH);

    private final MethodAnalyzer methodAnalyzer = new MethodAnalyzer("all");

    private final Method testMethods[] = TestClass.class.getDeclaredMethods();
    private final Constructor testConstructors[] = TestClass.class.getDeclaredConstructors();

    @Test
    public void should_strip_modifiers_publicStaticMethod1()
        throws IOException, NoSuchMethodException {
        String s =
            stripModifiersAndReturnType(
                signatureToString(
                    makeSignature(TestClass.class, findTestMethod("publicStaticMethod1"))));
        assertThat(
            s,
            is(
                "public io.codekvast.javaagent.util.SignatureUtilsTest.TestClass.publicStaticMethod1(java.lang.String, java.util.Collection)"));
    }

    @Test
    public void should_strip_modifiers_protectedMethod2() throws IOException, NoSuchMethodException {
        String s =
            stripModifiersAndReturnType(
                signatureToString(makeSignature(TestClass.class, findTestMethod("protectedMethod2"))));
        assertThat(
            s,
            is(
                "protected io.codekvast.javaagent.util.SignatureUtilsTest.TestClass.protectedMethod2()"));
    }

    @Test
    public void should_strip_modifiers_privateMethod3() throws IOException, NoSuchMethodException {
        String s =
            stripModifiersAndReturnType(
                signatureToString(makeSignature(TestClass.class, findTestMethod("privateMethod3"))));
        assertThat(
            s,
            is(
                "private io.codekvast.javaagent.util.SignatureUtilsTest.TestClass.privateMethod3(int, java.lang.String[])"));
    }

    @Test
    public void should_strip_modifiers_privateMethod4() throws IOException, NoSuchMethodException {
        String s =
            stripModifiersAndReturnType(
                signatureToString(
                    makeSignature(TestClass.class, findTestMethod("packagePrivateMethod4"))));
        assertThat(
            s,
            is(
                "package-private io.codekvast.javaagent.util.SignatureUtilsTest.TestClass.packagePrivateMethod4(int)"));
    }

    @Test
    public void should_strip_modifiers_twice() throws NoSuchMethodException {
        String s =
            stripModifiersAndReturnType(
                signatureToString(makeSignature(TestClass.class, findTestMethod("privateMethod3"))));
        assertThat(
            s,
            is(
                "private io.codekvast.javaagent.util.SignatureUtilsTest.TestClass.privateMethod3(int, java.lang.String[])"));
        String s2 = stripModifiersAndReturnType(s);
        assertThat(s2, is(s));
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void should_make_signature_for_protectedMethod2() throws Exception {
        MethodSignature4 signature =
            makeMethodSignature(TestClass.class, findTestMethod("protectedMethod2"));
        assertThat(signature, notNullValue());
        assertThat(
            signature.getAspectjString(),
            is(
                "protected io.codekvast.javaagent.util.SignatureUtilsTest.TestClass.protectedMethod2()"));
        assertThat(
            signature.getDeclaringType(),
            is("io.codekvast.javaagent.util.SignatureUtilsTest$TestClass"));
        assertThat(signature.getExceptionTypes(), is(""));
        assertThat(signature.getMethodName(), is("protectedMethod2"));
        assertThat(signature.getModifiers(), is("protected"));
        assertThat(signature.getPackageName(), is("io.codekvast.javaagent.util"));
        assertThat(signature.getParameterTypes(), is(""));
        assertThat(signature.getReturnType(), is("java.lang.Integer"));
        assertThat(signature.getLocation(), matchesPattern(LOCATION_PATTERN));
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void should_make_signature_for_protectedMethod5() throws Exception {
        MethodSignature4 signature =
            makeMethodSignature(TestClass.class, findTestMethod("protectedMethod5"));
        assertThat(signature, notNullValue());
        assertThat(
            signature.getAspectjString(),
            is(
                "protected io.codekvast.javaagent.util.SignatureUtilsTest.TestClass.protectedMethod5(java.lang.String, io"
                    + ".codekvast"
                    + ".javaagent.util.SignatureUtilsTest.TestInterface)"));
        assertThat(
            signature.getDeclaringType(),
            is("io.codekvast.javaagent.util.SignatureUtilsTest$TestClass"));
        assertThat(signature.getExceptionTypes(), is("java.lang.UnsupportedOperationException"));
        assertThat(signature.getMethodName(), is("protectedMethod5"));
        assertThat(signature.getModifiers(), is("protected final strictfp"));
        assertThat(signature.getPackageName(), is("io.codekvast.javaagent.util"));
        assertThat(
            signature.getParameterTypes(),
            is("java.lang.String, io.codekvast.javaagent.util.SignatureUtilsTest$TestInterface"));
        assertThat(signature.getReturnType(), is("int"));
        assertThat(signature.getLocation(), matchesPattern(LOCATION_PATTERN));
    }

    @Test
    public void should_make_signature_for_TestClass_constructor() throws Exception {
        MethodSignature4 signature =
            makeConstructorSignature(TestClass.class, findTestConstructor("TestClass()"));
        assertThat(signature, notNullValue());
        assertThat(
            signature.getAspectjString(),
            is("public io.codekvast.javaagent.util.SignatureUtilsTest.TestClass()"));
        assertThat(
            signature.getDeclaringType(),
            is("io.codekvast.javaagent.util.SignatureUtilsTest$TestClass"));
        assertThat(signature.getExceptionTypes(), is(""));
        assertThat(signature.getMethodName(), is("<init>"));
        assertThat(signature.getModifiers(), is("public"));
        assertThat(signature.getPackageName(), is("io.codekvast.javaagent.util"));
        assertThat(signature.getParameterTypes(), is(""));
        assertThat(signature.getReturnType(), is(""));
        assertThat(signature.getLocation(), matchesPattern(LOCATION_PATTERN));
    }

    private Constructor findTestConstructor(String name) {
        for (Constructor ctor : testConstructors) {
            if (ctor.toString().contains(name)) {
                return ctor;
            }
        }
        throw new IllegalArgumentException("Unknown test constructor: " + name);
    }

    @Test
    public void should_make_signature_for_TestClass_constructor_int() throws Exception {
        MethodSignature4 signature =
            makeConstructorSignature(TestClass.class, findTestConstructor("TestClass(int)"));
        assertThat(signature, notNullValue());
        assertThat(
            signature.getAspectjString(),
            is("protected io.codekvast.javaagent.util.SignatureUtilsTest.TestClass(int)"));
        assertThat(
            signature.getDeclaringType(),
            is("io.codekvast.javaagent.util.SignatureUtilsTest$TestClass"));
        assertThat(signature.getExceptionTypes(), is(""));
        assertThat(signature.getMethodName(), is("<init>"));
        assertThat(signature.getModifiers(), is("protected"));
        assertThat(signature.getPackageName(), is("io.codekvast.javaagent.util"));
        assertThat(signature.getParameterTypes(), is("int"));
        assertThat(signature.getReturnType(), is(""));
        assertThat(signature.getLocation(), matchesPattern(LOCATION_PATTERN));
    }

    @Test
    public void should_make_signature_for_TestClass_constructor_int_int() throws Exception {
        MethodSignature4 signature =
            makeConstructorSignature(TestClass.class, findTestConstructor("TestClass(int,int)"));
        assertThat(signature, notNullValue());
        assertThat(
            signature.getAspectjString(),
            is("package-private io.codekvast.javaagent.util.SignatureUtilsTest.TestClass(int, int)"));
        assertThat(
            signature.getDeclaringType(),
            is("io.codekvast.javaagent.util.SignatureUtilsTest$TestClass"));
        assertThat(signature.getExceptionTypes(), is("java.lang.UnsupportedOperationException"));
        assertThat(signature.getMethodName(), is("<init>"));
        assertThat(signature.getModifiers(), is(""));
        assertThat(signature.getPackageName(), is("io.codekvast.javaagent.util"));
        assertThat(signature.getParameterTypes(), is("int, int"));
        assertThat(signature.getReturnType(), is(""));
        assertThat(signature.getLocation(), matchesPattern(LOCATION_PATTERN));
    }

    @Test
    public void should_strip_modifiers_when_no_lparen() throws Exception {
        String signature = "public synchronized java.lang.String com.acme.Foo.bar";
        assertThat(stripModifiers(signature), is("com.acme.Foo.bar"));
    }

    @Test
    public void should_strip_modifiers_when_lparen() throws Exception {
        String signature = "public synchronized java.lang.String com.acme.Foo.bar()";
        assertThat(stripModifiers(signature), is("com.acme.Foo.bar()"));
    }

    @Test
    public void should_strip_modifiers_and_return_type_when_no_lparen() throws Exception {
        String signature = "public synchronized java.lang.String foobar";
        assertThat(stripModifiersAndReturnType(signature), is("public foobar"));
    }

    @Test
    public void should_detect_visibilities() {
        assertThat(SignatureUtils.getVisibility("foo public bar"), is("public"));
        assertThat(SignatureUtils.getVisibility("foo protected bar"), is("protected"));
        assertThat(SignatureUtils.getVisibility("foo package-private bar"), is("package-private"));
        assertThat(SignatureUtils.getVisibility("foo private bar"), is("private"));
        assertThat(SignatureUtils.getVisibility("foo bar"), is("package-private"));
    }

    @Test
    public void should_handle_null_codeSource() throws NoSuchMethodException {
        val clazz = BigInteger.class;
        assertThat(makeLocation(clazz), is(nullValue()));

        MethodLocation4 sigLoc =
            makeMethodLocation(
                makeSignature(clazz, findMethod(clazz, "nextProbablePrime")), makeLocation(clazz));
        assertThat(
            sigLoc,
            is(
                new MethodLocation4(
                    "public java.math.BigInteger java.math.BigInteger.nextProbablePrime()", null)));
    }

    @Test
    public void should_handle_jarred_location() throws NoSuchMethodException {
        val clazz = AbstractAssert.class;
        MethodLocation4 sigLoc =
            makeMethodLocation(makeSignature(clazz, findMethod(clazz, "isNull")), makeLocation(clazz));
        assertThat(
            sigLoc,
            is(
                new MethodLocation4(
                    "public void org.assertj.core.api.AbstractAssert.isNull()",
                    "assertj-core-2.6.0.jar")));
        assertThat(
            stripModifiersAndReturnType(sigLoc.getSignature()),
            is("public org.assertj.core.api.AbstractAssert.isNull()"));
    }

    private Method findTestMethod(String name) {
        for (Method method : testMethods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown test method: " + name);
    }

    private Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        return clazz.getDeclaredMethod(name, parameterTypes);
    }

    @SuppressWarnings("unused")
    public interface TestInterface {
        void foo();
    }

    @SuppressWarnings({"EmptyMethod", "unused"})
    public static class TestClass {
        private final int i;
        private final int j;

        public TestClass() {
            this(0, 0);
        }

        TestClass(int i, int j) throws UnsupportedOperationException {
            this.i = i;
            this.j = j;
        }

        protected TestClass(int i) {
            this(i, 0);
        }

        public static Collection<List<String>> publicStaticMethod1(String p1, Collection<Integer> p2) {
            return null;
        }

        protected Integer protectedMethod2() {
            return null;
        }

        private String privateMethod3(int p1, String... args) {
            return null;
        }

        int packagePrivateMethod4(int p1) {
            return 0;
        }

        @SuppressWarnings("FinalMethod")
        protected final strictfp int protectedMethod5(String p1, TestInterface p2)
            throws UnsupportedOperationException {
            return 0;
        }
    }
}

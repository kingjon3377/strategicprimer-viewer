"A module for utility methods, objects, and classes that only work on the JVM"
// FIXME: add license() annotations throughout
native("jvm")
module lovelace.util.jvm "0.1.0" {
    import ceylon.math "1.3.2";
    import ceylon.collection "1.3.2";
    shared import java.base "8";
    shared import java.desktop "8";
    shared import lovelace.util.common "0.1.0";
    import ceylon.interop.java "1.3.2";
    import ceylon.test "1.3.2";
}

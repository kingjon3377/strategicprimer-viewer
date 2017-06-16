"A module for utility methods, objects, and classes that only work on the JVM"
license("GPL-3")
native("jvm")
module lovelace.util.jvm "0.1.0" {
    import ceylon.math "1.3.2";
    import ceylon.collection "1.3.2";
    shared import java.base "8";
    shared import java.desktop "8";
    shared import lovelace.util.common "0.1.0";
    import ceylon.interop.java "1.3.2";
    // TODO: Uncomment tests once Ceylon bug #6986 is fixed
//    import ceylon.test "1.3.2";
    shared import javax.xml "8";
    shared import ceylon.random "1.3.2";
    import ceylon.logging "1.3.2";
    import ceylon.file "1.3.2";
}

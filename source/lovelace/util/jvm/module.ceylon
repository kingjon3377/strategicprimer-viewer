"A module for utility methods, objects, and classes that only work on the JVM"
license("GPL-3")
native("jvm")
module lovelace.util.jvm "0.1.0" {
    value ceylonVersion = "1.3.3";
    value javaVersion = "8";
    import ceylon.collection ceylonVersion;
    shared import java.base javaVersion;
    shared import java.desktop javaVersion;
    shared import lovelace.util.common "0.1.0";
    import ceylon.interop.java ceylonVersion;
    // TODO: Uncomment tests once Ceylon bug #6986 is fixed
//    import ceylon.test ceylonVersion;
    shared import javax.xml javaVersion;
    import ceylon.logging ceylonVersion;
    shared import ceylon.file ceylonVersion;
    import ceylon.io ceylonVersion;
    shared import ceylon.buffer ceylonVersion;
}

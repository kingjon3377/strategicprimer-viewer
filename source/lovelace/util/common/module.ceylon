"A collection of utility functions that don't require any Java-only types in their
 signatures or (eclipse/ceylon#6986) Java AWT or Swing in their implementation."
by("Jonathan Lovelace")
license("GPL-3")
todo("Once Ceylon bug #6986 is fixed, combine with `lovelace.util.jvm` in one module,
      with only the parts requiring the JVM marked as `native`.")
module lovelace.util.common "0.1.0" {
    value ceylonVersion = "1.3.3";
    shared import ceylon.collection ceylonVersion;
    shared import ceylon.test ceylonVersion;
    import ceylon.whole ceylonVersion;
    native("jvm")
    import ceylon.decimal ceylonVersion;
    shared import ceylon.random ceylonVersion;
    native("jvm") import java.base "8";
    import ceylon.logging ceylonVersion;
}

"A collection of pure-Ceylon utility functions"
by("Jonathan Lovelace")
license("GPL-3")
todo("Once Ceylon bug #6986 is fixed, combine with `lovelace.util.jvm` in one module,
      with only the parts requiring the JVM marked as `native`.")
module lovelace.util.common "0.1.0" {
    value ceylonVersion = "1.3.3";
    shared import ceylon.collection ceylonVersion;
    shared import ceylon.test ceylonVersion;
    import ceylon.whole "1.3.3";
    native("jvm")
    import ceylon.decimal "1.3.3";
}

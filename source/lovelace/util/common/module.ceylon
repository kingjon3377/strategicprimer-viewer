"A collection of pure-Ceylon utility functions"
by("Jonathan Lovelace")
license("GPL-3")
todo("Once Ceylon bug #6986 is fixed, combine with `lovelace.util.jvm` in one module,
      with only the parts requiring the JVM marked as `native`.")
module lovelace.util.common "0.1.0" {
    shared import ceylon.collection "1.3.3";
    import ceylon.test "1.3.3";
}

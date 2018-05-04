"Common functionality needed by pretty much all of the [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) assistive programs,
 one level up from `strategicprimer.model`."
license("GPL-3")
// TODO: add tagged() annotations throughout
native("jvm") // TODO: Make only the parts actually using JVM-specific idioms "native".
module strategicprimer.drivers.common "0.4.9016" {
    value ceylonVersion = "1.3.3";
    value lovelaceUtilsVersion = "0.1.0";
    shared import strategicprimer.model "0.4.9016";
    import ceylon.logging ceylonVersion;
    shared import lovelace.util.common lovelaceUtilsVersion;
    import ceylon.interop.java ceylonVersion;
    import ceylon.test ceylonVersion;
    import lovelace.util.jvm lovelaceUtilsVersion;
    import ceylon.random ceylonVersion;
	shared import ceylon.decimal ceylonVersion;
}

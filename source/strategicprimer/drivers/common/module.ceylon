"Common functionality needed by pretty much all of the [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) assistive programs,
 one level up from `strategicprimer.model.common`."
license("GPL-3")
// TODO: add tagged() annotations throughout
// TODO: Write user-introductory documentation of this module
native("jvm") // TODO: Remove once ceylon.decimal becomes cross-platform, eclipse/ceylon#2448
module strategicprimer.drivers.common "0.4.9017" {
    value ceylonVersion = "1.3.3";
    value lovelaceUtilsVersion = "0.1.0";
    shared import strategicprimer.model.common "0.4.9017";
    import ceylon.logging ceylonVersion;
    shared import lovelace.util.common lovelaceUtilsVersion;
    import ceylon.test ceylonVersion;
    import ceylon.random ceylonVersion;
    shared import ceylon.decimal ceylonVersion;
}

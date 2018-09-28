"Model objects, tests, and debugging methods for the old, table-based,
 encounter/exploration-results model of [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer)"
license("GPL-3")
native("jvm")
module strategicprimer.drivers.exploration.old "0.4.9017" {
    value ceylonVersion = "1.3.3";
    value lovelaceUtilsVersion = "0.1.0";
    value spVersion = "0.4.9017";
    shared import strategicprimer.model.impl spVersion;
    import ceylon.collection ceylonVersion;
    import ceylon.test ceylonVersion;
    shared import ceylon.file ceylonVersion;
    import ceylon.logging ceylonVersion;
    shared import strategicprimer.drivers.common spVersion;
    import lovelace.util.common lovelaceUtilsVersion;
    import lovelace.util.jvm lovelaceUtilsVersion;
}

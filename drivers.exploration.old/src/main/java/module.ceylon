"Model objects, tests, and debugging methods for the old, table-based,
 encounter/exploration-results model of [Strategic
 Primer](https://strategicprimer.wordpress.com)"
// TODO: Write user-introductory documentation for the module
license("GPL-3")
native("jvm")
module strategicprimer.drivers.exploration.old "0.4.9019" {
    value ceylonVersion = "1.3.3";
    value lovelaceUtilsVersion = "0.1.1";
    value spVersion = "0.4.9019";
    shared import strategicprimer.model.common spVersion;
    import ceylon.collection ceylonVersion;
    import ceylon.test ceylonVersion;
    shared import ceylon.file ceylonVersion;
    import ceylon.logging ceylonVersion;
    shared import strategicprimer.drivers.common spVersion;
    import lovelace.util.common lovelaceUtilsVersion;
}

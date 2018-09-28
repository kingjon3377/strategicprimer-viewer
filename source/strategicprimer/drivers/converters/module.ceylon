"Various converters for the [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) assistive programs
 suite."
license("GPL-3")
native("jvm")
module strategicprimer.drivers.converters "0.4.9017" {
    value ceylonVersion = "1.3.3";
    value lovelaceUtilsVersion = "0.1.0";
    value spVersion = "0.4.9017";
    import ceylon.collection ceylonVersion;
    shared import strategicprimer.model.impl spVersion;
    import ceylon.test ceylonVersion;
    import lovelace.util.common lovelaceUtilsVersion;
    import lovelace.util.jvm lovelaceUtilsVersion;
    import strategicprimer.drivers.common spVersion;
    import ceylon.logging ceylonVersion;
    import ceylon.regex ceylonVersion;
    import ceylon.file ceylonVersion;
    shared import strategicprimer.drivers.exploration.old spVersion;
    import ceylon.random ceylonVersion;
}

"Model objects, tests, and debugging methods for the old, table-based,
 encounter/exploration-results model of [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer)"
native("jvm")
module strategicprimer.drivers.exploration.old "0.4.9900" {
    shared import strategicprimer.model "0.4.9900";
    import ceylon.collection "1.3.2";
    import ceylon.test "1.3.2";
    shared import ceylon.file "1.3.2";
    import ceylon.logging "1.3.2";
    import strategicprimer.drivers.common "0.4.9900";
    import lovelace.util.common "0.1.0";
    import lovelace.util.jvm "0.1.0";
}

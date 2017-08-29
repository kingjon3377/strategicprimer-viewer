"Model objects, tests, and debugging methods for the old, table-based,
 encounter/exploration-results model of [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer)"
license("GPL-3")
native("jvm")
module strategicprimer.drivers.exploration.old "0.4.9015" {
    shared import strategicprimer.model "0.4.9015";
    import ceylon.collection "1.3.3";
    import ceylon.test "1.3.3";
    shared import ceylon.file "1.3.3";
    import ceylon.logging "1.3.3";
    import strategicprimer.drivers.common "0.4.9015";
    import lovelace.util.common "0.1.0";
    import lovelace.util.jvm "0.1.0";
}

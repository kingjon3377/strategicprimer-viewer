"Various converters for the [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) assistive programs
 suite."
license("GPL-3")
native("jvm")
module strategicprimer.drivers.converters "0.4.9014" {
    import ceylon.collection "1.3.2";
    shared import strategicprimer.model "0.4.9014";
    import ceylon.test "1.3.2";
    import lovelace.util.common "0.1.0";
    import lovelace.util.jvm "0.1.0";
    import strategicprimer.drivers.common "0.4.9014";
    import ceylon.logging "1.3.2";
    import ceylon.regex "1.3.2";
    import ceylon.file "1.3.2";
    shared import strategicprimer.drivers.exploration.old "0.4.9014";
    import ceylon.random "1.3.2";
}

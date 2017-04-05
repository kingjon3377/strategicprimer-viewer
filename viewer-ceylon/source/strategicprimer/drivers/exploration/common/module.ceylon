"Common functionality shared between the exploration CLI and the exploration GUI."
native("jvm")
module strategicprimer.drivers.exploration.common "0.4.9900" {
    shared import strategicprimer.drivers.common "0.4.9900";
    import strategicprimer.model "0.4.9900";
    import ceylon.collection "1.3.2";
    import ceylon.math "1.3.2";
    import ceylon.test "1.3.2";
    import lovelace.util.jvm "0.1.0";
}

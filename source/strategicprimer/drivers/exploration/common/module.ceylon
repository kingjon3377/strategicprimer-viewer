"Common functionality shared between the exploration CLI and the exploration GUI."
license("GPL-3")
native("jvm")
module strategicprimer.drivers.exploration.common "0.4.9015" {
    shared import strategicprimer.drivers.common "0.4.9015";
    import strategicprimer.model "0.4.9015";
    import ceylon.collection "1.3.3";
    import ceylon.math "1.3.3";
    import ceylon.test "1.3.3";
    import lovelace.util.jvm "0.1.0";
    import ceylon.random "1.3.3";
}

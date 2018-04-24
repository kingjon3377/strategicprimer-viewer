"Common functionality shared between the exploration CLI and the exploration GUI."
license("GPL-3")
native("jvm")
module strategicprimer.drivers.exploration.common "0.4.9016" {
    value ceylonVersion = "1.3.3";
    value spVersion = "0.4.9016";
    shared import strategicprimer.drivers.common spVersion;
    import strategicprimer.model spVersion;
    import ceylon.collection ceylonVersion;
    import ceylon.numeric ceylonVersion;
    import ceylon.test ceylonVersion;
    import lovelace.util.jvm "0.1.0";
    import ceylon.random ceylonVersion;
	import ceylon.logging ceylonVersion;
}

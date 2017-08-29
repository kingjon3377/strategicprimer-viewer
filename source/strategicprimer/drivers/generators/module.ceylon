"Drivers, generally CLI, that generate map contents of one sort or another."
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.drivers.generators "0.4.9014" {
	shared import strategicprimer.drivers.common "0.4.9014";
	import strategicprimer.model "0.4.9014";
    import lovelace.util.jvm "0.1.0";
    import strategicprimer.drivers.exploration.common "0.4.9014";
    import strategicprimer.drivers.exploration.old "0.4.9014";
	import ceylon.logging "1.3.3";
}

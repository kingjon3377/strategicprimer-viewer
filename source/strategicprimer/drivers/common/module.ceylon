"Common functionality needed by pretty much all of the [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) assistive programs,
 one level up from `strategicprimer.model`."
license("GPL-3")
// TODO: add tagged() annotations throughout
native("jvm")
module strategicprimer.drivers.common "0.4.9015" {
	shared import strategicprimer.model "0.4.9015";
	import ceylon.logging "1.3.3";
	shared import lovelace.util.common "0.1.0";
	import ceylon.interop.java "1.3.3";
	import ceylon.test "1.3.3";
    import lovelace.util.jvm "0.1.0";
	import ceylon.random "1.3.3";
}

"Common classes used by both the worker-management and advancement apps from the
 [Strategic Primer](https://shinecycle.wordpress.com/archives/strategic-primer) assistive
 programs suite, and possibly even CLI versions Any class extending a Swing class should
 go elsewhere, at least until [the Ceylon tooling
 bug](https://github.com/ceylon/ceylon/issues/6986) is fixed."
license("GPL-3")
native("jvm")
module strategicprimer.drivers.worker.common "0.4.9016" {
    shared import java.base "8";
    import ceylon.collection "1.3.3";
    shared import strategicprimer.drivers.common "0.4.9016";
    shared import strategicprimer.model "0.4.9016";
    import ceylon.test "1.3.3";
    import lovelace.util.jvm "0.1.0";
    shared import java.desktop "8";
    import ceylon.logging "1.3.3";
    import ceylon.random "1.3.3";
}

"Common code for [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) GUIs."
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.drivers.gui.common "0.4.9014" {
    shared import java.desktop "8";
    shared import java.base "8";
    import lovelace.util.jvm "0.1.0";
    import ceylon.logging "1.3.2";
}

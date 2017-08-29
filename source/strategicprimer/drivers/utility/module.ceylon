""""Utility" apps for [Strategic
   Primer](https://shinecycle.wordpress.com/archives/strategic-primer)"""
// TODO: Make only what's needed native("jvm") instead of the whole module
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.drivers.utility "0.4.9015" {
    import ceylon.collection "1.3.3";
    import ceylon.math "1.3.3";
    shared import java.base "8";
    import strategicprimer.model "0.4.9015";
    import java.desktop "8";
    import ceylon.regex "1.3.3";
    import lovelace.util.jvm "0.1.0";
    import strategicprimer.drivers.gui.common "0.4.9015";
//    import maven:"com.pump:pump-swing" "1.0.00";
    import com.pump.swing "1.0.00";
    import maven:"com.massisframework:orange-extensions" "1.3.1";
    import ceylon.logging "1.3.3";
    shared import strategicprimer.drivers.common "0.4.9015";
}

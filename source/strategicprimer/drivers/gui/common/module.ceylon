"Common code for [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) GUIs."
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.drivers.gui.common "0.4.9016" {
    shared import java.desktop "8";
    shared import java.base "8";
    import lovelace.util.jvm "0.1.0";
    import ceylon.logging "1.3.3";
    import maven:"com.massisframework:orange-extensions" "1.3.1";
//    import maven:"com.pump:pump-swing" "1.0.00";
    import com.pump.swing "1.0.00";
    import ceylon.interop.java "1.3.3";
    import ceylon.regex "1.3.3";
}

"Common code for [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) GUIs."
// TODO: Write user-introductory documentation for this module
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.drivers.gui.common "0.4.9017" {
    value ceylonVersion = "1.3.3";
    value javaVersion = "8";
    shared import java.desktop javaVersion;
    shared import java.base javaVersion;
    shared import lovelace.util.common "0.1.0";
    shared import lovelace.util.jvm "0.1.0";
    import ceylon.logging ceylonVersion;
    import maven:"com.massisframework:orange-extensions" "1.3.1";
//    import maven:"com.pump:pump-swing" "1.0.00";
    import com.pump.swing "1.0.00";
    import ceylon.regex ceylonVersion;
    shared import strategicprimer.drivers.common "0.4.9017";
}

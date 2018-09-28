""""Utility" apps for [Strategic
   Primer](https://shinecycle.wordpress.com/archives/strategic-primer)"""
// TODO: Make only what's needed native("jvm") instead of the whole module
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.drivers.utility "0.4.9017" {
    value ceylonVersion = "1.3.3";
    value javaVersion = "8";
    value spVersion = "0.4.9017";
    import ceylon.collection ceylonVersion;
    import ceylon.decimal ceylonVersion;
    import ceylon.whole ceylonVersion;
    shared import java.base javaVersion;
    import strategicprimer.model.impl spVersion;
    import java.desktop javaVersion;
    import ceylon.regex ceylonVersion;
    import lovelace.util.jvm "0.1.0";
    import strategicprimer.drivers.gui.common spVersion;
//    import maven:"com.pump:pump-swing" "1.0.00";
    import com.pump.swing "1.0.00";
    import maven:"com.massisframework:orange-extensions" "1.3.1";
    import ceylon.logging ceylonVersion;
    shared import strategicprimer.drivers.common spVersion;
    import strategicprimer.drivers.exploration.old spVersion;
    import strategicprimer.drivers.exploration.common spVersion;
    import ceylon.random ceylonVersion;
}

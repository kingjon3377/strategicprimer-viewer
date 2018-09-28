"Assistive programs for players and Judges of
 [Strategic Primer](https://shinecycle.wordpress.com/archives/strategic-primer)"
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.viewer "0.4.9017" {
    value ceylonVersion = "1.3.3";
    value javaVersion = "8";
    value lovelaceUtilsVersion = "0.1.0";
    value spVersion = "0.4.9017";
    shared import java.base javaVersion;
    import ceylon.collection ceylonVersion;
    import ceylon.interop.java ceylonVersion;
    import ceylon.logging ceylonVersion;
    shared import lovelace.util.common lovelaceUtilsVersion;
    shared import javax.xml javaVersion;
    shared import ceylon.numeric ceylonVersion;
    shared import ceylon.file ceylonVersion;
//    import ceylon.io ceylonVersion;
//    import maven:"com.pump:pump-swing" "1.0.00";
    import com.pump.swing "1.0.00";
    shared import java.desktop javaVersion;
    shared import lovelace.util.jvm lovelaceUtilsVersion;
    import ceylon.regex ceylonVersion;
    import maven:"com.massisframework:orange-extensions" "1.3.1";
    shared import strategicprimer.model.impl spVersion;
    import strategicprimer.report spVersion;
    shared import strategicprimer.drivers.common spVersion;
    import strategicprimer.drivers.exploration.old spVersion;
    import strategicprimer.drivers.exploration.common spVersion;
    shared import strategicprimer.drivers.worker.common spVersion;
    import ceylon.random ceylonVersion;
    import strategicprimer.drivers.generators spVersion;
    shared import strategicprimer.drivers.gui.common spVersion;
    import strategicprimer.drivers.utility spVersion;
    import ceylon.http.server ceylonVersion;
    import com.vasileff.ceylon.structures "1.1.3";
    import strategicprimer.mining spVersion;
}

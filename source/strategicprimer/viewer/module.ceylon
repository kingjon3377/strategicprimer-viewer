"Assistive programs for players and Judges of [Strategic
 Primer](https://strategicprimer.wordpress.com)"
// TODO: Expand into user-introductory documentation for the suite
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.viewer "0.4.9019" {
    value ceylonVersion = "1.3.3";
    value javaVersion = "8";
    value lovelaceUtilsVersion = "0.1.1";
    value spVersion = "0.4.9019";
    shared import java.base javaVersion;
    import ceylon.collection ceylonVersion;
    import ceylon.interop.java ceylonVersion;
    import ceylon.logging ceylonVersion;
    shared import lovelace.util.common lovelaceUtilsVersion;
    shared import javax.xml javaVersion;
    shared import ceylon.numeric ceylonVersion;
    shared import ceylon.file ceylonVersion;
//    import ceylon.io ceylonVersion;
    import com.pump "0.0";
    shared import java.desktop javaVersion;
    shared import lovelace.util.jvm lovelaceUtilsVersion;
    import ceylon.regex ceylonVersion;
    import maven:"com.massisframework:orange-extensions" "1.3.1";
    shared import strategicprimer.model.impl spVersion;
    import strategicprimer.report spVersion;
    shared import strategicprimer.drivers.common spVersion;
    import strategicprimer.drivers.exploration.old spVersion;
    shared import strategicprimer.drivers.exploration.common spVersion;
    shared import strategicprimer.drivers.worker.common spVersion;
    import ceylon.random ceylonVersion;
    import strategicprimer.drivers.generators spVersion;
    shared import strategicprimer.drivers.gui.common spVersion;
    import strategicprimer.drivers.utility spVersion;
//    import ceylon.http.server ceylonVersion;
    import maven:"org.takes:takes" "1.19";
    import com.vasileff.ceylon.structures "1.1.3";
    import strategicprimer.mining spVersion;
    import strategicprimer.drivers.query spVersion;
}

"Assistive programs for players and Judges of
 [Strategic Primer](https://shinecycle.wordpress.com/archives/strategic-primer)"
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.viewer "0.4.9014" {
    shared import java.base "8";
    import ceylon.collection "1.3.3";
    import ceylon.interop.java "1.3.3";
    import ceylon.logging "1.3.3";
    shared import lovelace.util.common "0.1.0";
    shared import javax.xml "8";
    shared import ceylon.math "1.3.3";
    shared import ceylon.file "1.3.3";
//    import ceylon.io "1.3.3";
//    import maven:"com.pump:pump-swing" "1.0.00";
    import com.pump.swing "1.0.00";
    shared import java.desktop "8";
    import lovelace.util.jvm "0.1.0";
    import ceylon.regex "1.3.3";
    import maven:"com.massisframework:orange-extensions" "1.3.1";
    shared import strategicprimer.model "0.4.9014";
    import strategicprimer.report "0.4.9014";
    shared import strategicprimer.drivers.common "0.4.9014";
    import strategicprimer.drivers.exploration.old "0.4.9014";
    import strategicprimer.drivers.exploration.common "0.4.9014";
    shared import strategicprimer.drivers.worker.common "0.4.9014";
    import ceylon.random "1.3.3";
    import strategicprimer.drivers.generators "0.4.9014";
    shared import strategicprimer.drivers.gui.common "0.4.9014";
    import strategicprimer.drivers.utility "0.4.9014";
}

"Assistive programs for players and Judges of
 [Strategic Primer](https://shinecycle.wordpress.com/archives/strategic-primer)"
// FIXME: add license() annotations throughout
// TODO: add tagged() annotations throughout
native("jvm")
module strategicprimer.viewer "0.4.9900" {
    shared import java.base "8";
    import ceylon.collection "1.3.2";
    import ceylon.interop.java "1.3.2";
    import ceylon.logging "1.3.2";
    shared import lovelace.util.common "0.1.0";
    import ceylon.test "1.3.2";
    shared import javax.xml "8";
    shared import ceylon.math "1.3.2";
    shared import ceylon.file "1.3.2";
    import ceylon.io "1.3.2";
    import com.bric.window.windowmenu "1.0";
    shared import java.desktop "8";
    import lovelace.util.jvm "0.1.0";
    import ceylon.regex "1.3.2";
    import maven:"com.massisframework:orange-extensions" "1.3.1";
    shared import strategicprimer.model "0.4.9900";
    import strategicprimer.report "0.4.9900";
    shared import strategicprimer.drivers.common "0.4.9900";
}

"Functions to generate HTML and tabular (CSV) reports of the contents of [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) maps."
license("GPL-3")
native("jvm")
module strategicprimer.report "0.4.9016" {
    import lovelace.util.jvm "0.1.0";
    import ceylon.regex "1.3.3";
    import java.base "8";
    import ceylon.interop.java "1.3.3";
    shared import java.desktop "8";
    import ceylon.collection "1.3.3";
    import ceylon.logging "1.3.3";
    shared import strategicprimer.model "0.4.9016";
    shared import lovelace.util.common "0.1.0";
    import ceylon.test "1.3.3";
    shared import ceylon.file "1.3.3";
    import ceylon.random "1.3.3";
	import com.vasileff.ceylon.structures "1.1.3";
}

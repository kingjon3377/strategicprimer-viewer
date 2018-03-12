"Model objects, and their XML I/O, for the [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) assitive programs
 suite. Some of the converter apps, being tightly bound to XML I/O and to implementation
 details of the model, also have code in this module."
license("GPL-3")
// TODO: Make only the I/O parts "native"; blocked by eclipse/ceylon#7336
native("jvm")
module strategicprimer.model "0.4.9015" {
    shared import java.base "8";
    shared import javax.xml "8";
    import ceylon.test "1.3.3";
    shared import ceylon.collection "1.3.3";
    shared import lovelace.util.common "0.1.0";
    import ceylon.interop.java "1.3.3";
    import ceylon.logging "1.3.3";
    shared import ceylon.math "1.3.3";
    import lovelace.util.jvm "0.1.0";
    import ceylon.regex "1.3.3";
    shared import ceylon.random "1.3.3";
    shared import ceylon.file "1.3.3";
    import com.vasileff.ceylon.structures "1.1.3";
}

"Model objects, and their XML I/O, for the [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) assitive programs
 suite. Some of the converter apps, being tightly bound to XML I/O and to implementation
 details of the model, also have code in this module."
license("GPL-3")
// TODO: Make only the I/O parts "native"; blocked by eclipse/ceylon#7336
native("jvm")
module strategicprimer.model "0.4.9016" {
    value ceylonVersion = "1.3.3";
    value javaVersion = "8";
    value lovelaceUtilsVersion = "0.1.0";
    shared import java.base javaVersion;
    shared import javax.xml javaVersion;
    import ceylon.test ceylonVersion;
    shared import ceylon.collection ceylonVersion;
    shared import lovelace.util.common lovelaceUtilsVersion;
    import ceylon.interop.java ceylonVersion;
    import ceylon.logging ceylonVersion;
    shared import ceylon.numeric ceylonVersion;
    import lovelace.util.jvm lovelaceUtilsVersion;
    import ceylon.regex ceylonVersion;
    shared import ceylon.random ceylonVersion;
    shared import ceylon.file ceylonVersion;
    import com.vasileff.ceylon.structures "1.1.3";
    import ceylon.decimal ceylonVersion;
    import ceylon.whole ceylonVersion;
}

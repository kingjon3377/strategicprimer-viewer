"Pure-Ceylon model-object-related code for the [Strategic
 Primer](https://shinecycle.wordpress.com/archives/strategic-primer) assitive programs
 suite."
module strategicprimer.model.common "0.4.9017" {
    value ceylonVersion = "1.3.3";
    value lovelaceUtilsVersion = "0.1.0";
    import ceylon.logging ceylonVersion;
    import ceylon.numeric ceylonVersion;
    shared import lovelace.util.common lovelaceUtilsVersion;
    shared import ceylon.collection ceylonVersion;
    shared import ceylon.random ceylonVersion;
    shared import com.vasileff.ceylon.structures "1.1.3";
}
"An app to allow the user to get information about the map, including
 simulations of hunting, fishing, gathering, and trapping."
 // TODO: Improve user-introductory documentation
license("GPL-3")
native("jvm")
module strategicprimer.drivers.query "0.4.9018" {
    value ceylonVersion = "1.3.3";
    value spVersion = "0.4.9018";
    shared import strategicprimer.model.common spVersion;
    import ceylon.logging ceylonVersion;
    import ceylon.numeric ceylonVersion;
    shared import strategicprimer.drivers.common spVersion;
    import strategicprimer.drivers.exploration.common spVersion;
}

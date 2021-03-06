"Drivers, generally CLI, that generate map contents of one sort or another."
// TODO: Write user-introductory documentation
// TODO: add tagged() annotations throughout
license("GPL-3")
native("jvm")
module strategicprimer.drivers.generators "0.4.9019" {
    value ceylonVersion = "1.3.3";
    value spVersion = "0.4.9019";
    value lovelaceVersion = "0.1.1";
    shared import strategicprimer.drivers.common spVersion;
    import strategicprimer.model.common spVersion;
    import strategicprimer.drivers.exploration.common spVersion;
    import strategicprimer.drivers.exploration.old spVersion;
    import ceylon.logging ceylonVersion;
    import ceylon.decimal ceylonVersion;
    import lovelace.util.jvm lovelaceVersion;
}

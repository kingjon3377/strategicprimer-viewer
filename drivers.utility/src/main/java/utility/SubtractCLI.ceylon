import strategicprimer.drivers.common {
    CLIDriver,
    emptyOptions,
    SPOptions
}

"An app to produce a difference between two maps, to aid understanding what an explorer has
 found. This modifies non-main maps in place; only run on copies or under version control!"
shared class SubtractCLI(shared actual UtilityDriverModel model) satisfies CLIDriver {
    shared actual SPOptions options = emptyOptions;

    shared actual void startDriver() {
        for (loc in model.map.locations) {
            model.subtractAtPoint(loc);
        }
    }
}

import controller.map.drivers { ... }
import ceylon.collection {
    MutableMap,
    HashMap
}
import ceylon.logging {
    Logger,
    logger
}
import java.lang {
    System
}
import javax.swing {
    UIManager
}
Logger log = logger(`module strategicprimer.viewer`);
Map<String, ISPDriver[2]> createCache() {
    MutableMap<String, [ISPDriver, ISPDriver]> cache =
            HashMap<String, [ISPDriver, ISPDriver]>();
    void choices(ISPDriver cliDriver, ISPDriver guiDriver) {
        IDriverUsage cliUsage = cliDriver.usage();
        IDriverUsage guiUsage = guiDriver.usage();
        if (cliUsage.graphical) {
            log.warn("``cliUsage.shortDescription`` is GUI but passed as CLI");
        }
        if (!guiUsage.graphical) {
            log.warn("``guiUsage.shortDescription`` is CLI but passed as GUI");
        }
        if (cliUsage.shortOption != guiUsage.shortOption) {
            log.warn("Short options don't match between ``cliUsage.shortDescription`` and ``guiUsage.shortDescription``");
        }
        if (cliUsage.longOption != guiUsage.longOption) {
            log.warn("Long options don't match between ``cliUsage.shortDescription`` and ``guiUsage.shortDescription``");
        }
        cache.put(cliUsage.shortOption, [cliDriver, guiDriver]);
        cache.put(cliUsage.longOption, [cliDriver, guiDriver]);
    }
    void choice(ISPDriver driver) {
        IDriverUsage usage = driver.usage();
        cache.put(usage.shortOption, [driver, driver]);
        cache.put(usage.longOption, [driver, driver]);
    }
    choices(WorkerReportDriver(), ViewerStart());
    choices(AdvancementCLIDriver(), AdvancementStart());
    choices(StrategyExportDriver(), WorkerStart());
    choices(ExplorationCLIDriver(), ExplorationGUI());
    choices(ReaderComparator(), DrawHelperComparator());
    choices(MapChecker(), MapCheckerGUI());
    choices(SubsetDriver(), SubsetGUIDriver());
    // FIXME: Write GUI equivalent of QueryCLI
    choice(QueryCLI());
    choice(EchoDriver());
    // FIXME: Write GUI for the duplicate fixture remover
    choice(DuplicateFixtureRemoverCLI());
    // FIXME: Write trapping (and hunting, etc.) GUI
    choice(TrapModelDriver());
    // TODO: AppStarter went here
    // FIXME: Write stat-generating/stat-entering GUI
    choice(StatGeneratingCLIDriver());
    // FIXME: Write GUI for map-expanding driver
    choice(ExpansionDriver());
    // TODO: Write GUI equivalent of MapPopulatorDriver
    choice(MapPopulatorDriver());
    choices(ResourceAddingCLIDriver(), ResourceAddingGUIDriver());
    // TODO: Write GUI equivalent of TabularReportDriver
    choice(TabularReportDriver());
    // TODO: Write GUI to allow user to visually explore a mine
    choice(MiningCLI());
    return cache;
}
Map<String, ISPDriver[2]> driverCache = createCache();
void run() {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        "SP Helpers");
    System.setProperty("apple.awt.application.name", "SP Helpers");
    UIManager.setLookAndFeel(UIManager.systemLookAndFeelClassName);
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    SPOptionsImpl options = SPOptionsImpl();
    try {
        AppStarter().startDriver(options, *process.arguments);
    } catch (IncorrectUsageException except) {
        IDriverUsage usage = except.correctUsage;
        StringBuilder builder = StringBuilder();
        // FIXME: should open with either "ceylon run" or "java -jar /path/to/fat.jar"
        // and this module's name.
        builder.append("Usage: java controller.map.drivers.AppStarter ");
        if (usage.graphical) {
            builder.append("[-g|--gui]");
        } else {
            builder.append("-c|--cli");
        }
        builder.append(" ``usage.shortOption``|``usage.longOption``");
        for (option in usage.supportedOptions) {
            builder.append(" [``option``");
        }
        switch (usage.paramsWanted)
        case (ParamCount.none) {}
        case (ParamCount.one) { builder.append(" ``usage.firstParamDesc``"); }
        case (ParamCount.atLeastOne) {
            builder.append(" ``usage.firstParamDesc`` [``usage.subsequentParamDesc`` ...]");
        }
        case (ParamCount.two) {
            builder.append(" ``usage.firstParamDesc`` ``usage.subsequentParamDesc``");
        }
        case (ParamCount.atLeastTwo) {
            builder.append(" ``usage.firstParamDesc`` ``
                usage.subsequentParamDesc`` [``usage.subsequentParamDesc`` ...]");
        }
        case (ParamCount.anyNumber) {
            builder.append(" [``usage.subsequentParamDesc`` ...]");
        }
        builder.appendNewline();
        if (options.hasOption("--verbose")) {
            builder.append(usage.longDescription);
        } else {
            builder.append(usage.shortDescription);
        }
        process.writeErrorLine(builder.string);
        process.exit(1);
    } catch (DriverFailedException except) {
        log.error(except.message, except.cause);
        process.exit(2);
    }
}
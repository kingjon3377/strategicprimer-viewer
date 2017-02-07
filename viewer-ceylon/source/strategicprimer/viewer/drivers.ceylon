import controller.map.drivers { ... }
import ceylon.collection {
    MutableMap,
    HashMap,
    ArrayList,
    MutableList
}
import ceylon.logging {
    Logger,
    logger
}
import java.lang {
    System
}
import javax.swing {
    UIManager,
    SwingUtilities
}
import java.io {
    IOException
}
import controller.map.misc {
    CLIHelper, ICLIHelper
}
import view.util {
    AppChooserFrame
}
import model.misc {
    IDriverModel
}
import java.awt {
    GraphicsEnvironment
}
import ceylon.interop.java {
    JavaList
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
String usageMessage(IDriverUsage usage, Boolean verbose) {
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
    if (verbose) {
        builder.append(usage.longDescription);
    } else {
        builder.append(usage.shortDescription);
    }
    return builder.string;
}
object appStarter satisfies ISPDriver {
    shared actual void startDriver(ICLIHelper cli, SPOptions options, String?* args) {
        variable Boolean gui = !GraphicsEnvironment.headless;
        variable SPOptionsImpl currentOptions = SPOptionsImpl(options);
        if (!currentOptions.hasOption("--gui")) {
            currentOptions.setOption("--gui", gui.string);
        }
        MutableList<String> others = ArrayList<String>();
        variable [ISPDriver, ISPDriver]? currentDrivers = null;
        for (arg in args.coalesced) {
            if (arg == "-g" || arg == "--gui") {
                currentOptions.setOption("--gui", "true");
                gui = true;
            } else if (arg == "-c" || arg == "--cli") {
                currentOptions.setOption("--gui", "false");
                gui = false;
            } else if (arg.startsWith("--gui=")) {
                String tempString = arg.substring(6);
                value tempBool = Boolean.parse(tempString);
                if (is Boolean tempBool) {
                    currentOptions.setOption("--gui", tempString);
                    gui = tempBool;
                } else {
                    throw DriverFailedException("--gui=nonBoolean", tempBool);
                }
            } else if (arg.startsWith("-") && arg.contains("=")) {
                {String+} broken = arg.split('='.equals, true, false);
                currentOptions.setOption(broken.first, broken.rest.reduce<String>(
                    (String partial, String element) =>
                        if (partial.empty) then element else "``partial``=``element``"));
            } else if (driverCache.defines(arg.lowercased)) {
                if (exists temp = currentDrivers) {
                    SPOptions currentOptionsTyped = currentOptions;
                    if (gui) {
                        // TODO: catch and log a DriverFailedException inside the lambda
                        SwingUtilities.invokeLater(() =>
                                temp.rest.first.startDriver(cli, currentOptionsTyped,
                                    *others));
                    } else {
                        temp.first.startDriver(cli, currentOptionsTyped, *others);
                    }
                }
            }
        }
    }

    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel driverModel) {
        // TODO: what about -c?
        if (GraphicsEnvironment.headless) {
            List<ISPDriver> cliDrivers = ArrayList<ISPDriver>(driverCache.size,
                1.5, driverCache.items.map((element) => element.first));
            try {
                assert (is CLIHelper cli);
                if (exists driver = cliDrivers.get(cli.chooseFromList(JavaList(cliDrivers),
                        "CLI apps available:", "No applications available", "App to start: ",
                        true))) {
                    driver.startDriver(cli, options, driverModel);
                }
            } catch (IOException except) {
                log.error("I/O error prompting user for app to start", except);
            }
        } else {
            SwingUtilities.invokeLater(
                () => AppChooserFrame(cli, driverModel, options).setVisible(true));
        }
    }


}
shared void run() {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        "SP Helpers");
    System.setProperty("apple.awt.application.name", "SP Helpers");
    UIManager.setLookAndFeel(UIManager.systemLookAndFeelClassName);
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    SPOptionsImpl options = SPOptionsImpl();
    try {
        appStarter.startDriver(options, *process.arguments);
    } catch (IncorrectUsageException except) {
        IDriverUsage usage = except.correctUsage;
        process.writeErrorLine(usageMessage(usage, options.hasOption("--verbose")));
        process.exit(1);
    } catch (IOException|DriverFailedException except) {
        log.error(except.message, except.cause);
        process.exit(2);
    }
}
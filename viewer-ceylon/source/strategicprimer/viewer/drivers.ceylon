import controller.map.drivers { ... }
import ceylon.collection {
    MutableMap,
    HashMap,
    ArrayList,
    MutableList
}
import ceylon.logging {
    Logger,
    logger,
    addLogWriter,
    Priority
}
import java.lang {
    System,
    Runnable
}
import javax.swing {
    UIManager,
    SwingUtilities,
    JButton,
    JPanel,
    JScrollPane,
    JLabel
}
import java.io {
    IOException
}
import controller.map.misc {
    CLIHelper, ICLIHelper,
    DuplicateFixtureRemover
}
import view.util {
    ErrorShower,
    SPFrame,
    BorderedPanel
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import java.awt {
    GraphicsEnvironment,
    GridLayout
}
import ceylon.interop.java {
    JavaList
}
import ceylon.language.meta.declaration {
    Package,
    Module
}
import java.util {
    JList = List,
    Optional
}
import java.awt.event {
    ActionListener,
    ActionEvent
}
import java.nio.file {
    Path
}
import strategicprimer.viewer {
    advancementCLI,
    advancementGUI,
    drawHelperComparator
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"The method to actually write log messages to stderr."
void logWriter(Priority priority, Module|Package mod,
        String message, Throwable? except) {
    process.writeErrorLine("``priority`` (``mod``): ``message``");
    if (exists except) {
        process.writeErrorLine(except.message);
        except.printStackTrace();
    }
}
"Create the cache of driver objects."
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
    choices(advancementCLI, advancementGUI);
    choices(StrategyExportDriver(), WorkerStart());
    choices(explorationCLI, explorationGUI);
    choices(readerComparator, drawHelperComparator);
    choices(MapChecker(), MapCheckerGUI());
    choices(SubsetDriver(), SubsetGUIDriver());
    // FIXME: Write GUI equivalent of QueryCLI
    choice(QueryCLI());
    choice(echoDriver);
    // FIXME: Write GUI for the duplicate fixture remover
    choice(duplicateFixtureRemoverCLI);
    // FIXME: Write trapping (and hunting, etc.) GUI
    choice(TrapModelDriver());
    // TODO: AppStarter went here
    // FIXME: Write stat-generating/stat-entering GUI
    choice(StatGeneratingCLIDriver());
    // FIXME: Write GUI for map-expanding driver
    choice(expansionDriver);
    // TODO: Write GUI equivalent of MapPopulatorDriver
    choice(MapPopulatorDriver());
    choices(ResourceAddingCLIDriver(), ResourceAddingGUIDriver());
    // TODO: Write GUI equivalent of TabularReportDriver
    choice(TabularReportDriver());
    // TODO: Write GUI to allow user to visually explore a mine
    choice(MiningCLI());
    return cache;
}
"Create the usage message for a particular driver."
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
shared void run() {
    addLogWriter(logWriter);
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        "SP Helpers");
    System.setProperty("apple.awt.application.name", "SP Helpers");
    UIManager.setLookAndFeel(UIManager.systemLookAndFeelClassName);
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    SPOptionsImpl options = SPOptionsImpl();
    Map<String, ISPDriver[2]> driverCache = createCache();
    object appStarter satisfies ISPDriver {
        shared actual void startDriver(ICLIHelper cli, SPOptions options, String?* args) {
            log.info("Inside appStarter.startDriver()");
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
                    currentDrivers = driverCache.get(arg.lowercased);
                } else if (arg.startsWith("-")) {
                    currentOptions.addOption(arg);
                } else {
                    others.add(arg);
                }
            }
            if (options.hasOption("--help")) {
                IDriverUsage tempUsage;
                if (exists drivers = currentDrivers) {
                    if (gui) {
                        tempUsage = drivers.rest.first.usage();
                    } else {
                        tempUsage = drivers.first.usage();
                    }
                } else {
                    tempUsage = usage();
                }
                usageMessage(tempUsage, options.getArgument("--verbose") == "true");
            } else if (exists drivers = currentDrivers) {
                SPOptions currentOptionsTyped = currentOptions;
                if (gui) {
                    // TODO: catch and log a DriverFailedException inside the lambda
                    SwingUtilities.invokeLater(() =>
                    drivers.rest.first.startDriver(cli, currentOptionsTyped,
                    *others));
                } else {
                    drivers.first.startDriver(cli, currentOptionsTyped, *others);
                }
            } else {
                SPOptions currentOptionsTyped = currentOptions;
                if (gui) {
                    try {
                        SwingUtilities.invokeLater(() => appChooserFrame(cli,
                            currentOptionsTyped, others).setVisible(true));
                    } catch (DriverFailedException except) {
                        log.fatal(except.message, except);
                        SwingUtilities.invokeLater(() => ErrorShower.showErrorDialog(null, except.message));
                    }
                } else {
                    JList<ISPDriver> driversList = JavaList(ArrayList(driverCache.size,
                        1.0, driverCache.items.map(Tuple.first)));
                    Integer choice = cli.chooseFromList(driversList,
                        "CLI apps available:", "No applications available", "App to start: ", true);
                    if (choice >= 0 && choice < driversList.size()) {
                        driversList.get(choice).startDriver(cli, options, *others);
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
                    () => appChooserFrame(cli, options, driverModel).setVisible(true));
            }
        }
    }
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
SPFrame appChooserFrame(ICLIHelper cli, SPOptions options,
        {String*}|IDriverModel finalArg) {
    object frame extends SPFrame("SP App Chooser", Optional.empty<Path>()) {
        shared actual String windowName = "SP App Chooser";
    }
    JButton button(String desc, ISPDriver() target) {
        object retval extends JButton(desc) satisfies ActionListener&Runnable {
            shared actual void actionPerformed(ActionEvent event) {
                if (is IDriverModel finalArg) {
                    target().startDriver(cli, options, finalArg);
                } else {
                    target().startDriver(cli, options, *finalArg);
                }
                SwingUtilities.invokeLater(this);
            }
            shared actual void run() {
                frame.setVisible(false);
                frame.dispose();
            }
        }
        retval.addActionListener(retval);
        return retval;
    }
    JPanel buttonPanel = JPanel(GridLayout(0, 1));
    buttonPanel.add(button("Map Viewer", ViewerStart));
    buttonPanel.add(button("Worker Skill Advancement", () => advancementGUI));
    buttonPanel.add(button("Unit Orders and Worker Management", WorkerStart));
    buttonPanel.add(button("Exploration", () => explorationGUI));
    frame.contentPane = BorderedPanel(JScrollPane(buttonPanel),
        JLabel("Please choose one of the applications below"), null, null, null);
    frame.pack();
    return frame;
}
"""A driver to remove duplicate hills, forests, etc. from the map (to reduce the size it
   takes up on disk and the memory and CPU it takes to deal with it)."""
object duplicateFixtureRemoverCLI satisfies SimpleCLIDriver {
    DriverUsage usageObject = DriverUsage(false, "-u", "--duplicates", ParamCount.one,
        "Remove duplicate fixtures",
        "Remove duplicate fixtures (identical except ID# and on the same tile) from a map."
    );
    usageObject.addSupportedOption("--current-turn=NN");
    shared actual IDriverUsage usage() => usageObject;
    "Run the driver"
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        try {
            if (is IMultiMapModel model) {
                for (pair in model.allMaps) {
                    DuplicateFixtureRemover.filter(pair.first(), cli);
                }
            } else {
                DuplicateFixtureRemover.filter(model.map, cli);
            }
        } catch (IOException except) {
            throw DriverFailedException("I/O error interacting with user", except);
        }
    }
}
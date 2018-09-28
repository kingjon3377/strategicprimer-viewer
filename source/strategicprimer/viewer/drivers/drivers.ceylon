import ceylon.collection {
    MutableMap,
    HashMap,
    ArrayList,
    MutableList
}
import ceylon.language.meta.declaration {
    Package,
    Module
}
import ceylon.logging {
    Logger,
    logger,
    addLogWriter,
    Priority,
    trace,
    debug,
    defaultPriority
}

import java.awt {
    GraphicsEnvironment,
    GridLayout,
    Dimension,
    Graphics2D
}
import java.io {
    IOException,
    FileNotFoundException
}
import java.lang {
    System
}

import javax.swing {
    UIManager,
    SwingUtilities,
    JPanel,
    JScrollPane,
    JLabel,
    JEditorPane
}

import lovelace.util.jvm {
    showErrorDialog,
    BorderedPanel,
    platform,
    listenedButton
}

import strategicprimer.drivers.common {
    IDriverModel,
    ISPDriver,
    IDriverUsage,
    SPOptions,
    ParamCount,
    DriverFailedException,
    IncorrectUsageException,
    SPOptionsImpl
}
import strategicprimer.drivers.common.cli {
    ICLIHelper,
    CLIHelper
}
import com.apple.eawt {
    AppEvent,
    Application
}
import com.pump.window {
    WindowList
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    UtilityMenu,
    WindowCloseListener
}
import strategicprimer.model.impl.xmlio {
    SPFormatException
}
import lovelace.util.common {
    todo,
    defer,
    silentListener
}
import com.vasileff.ceylon.structures {
    MutableMultimap,
    ArrayListMultimap
}
import java.awt.image {
    BufferedImage
}
import java.nio.file {
    NoSuchFileException
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
object appChooserState {
    "The method to actually write log messages to stderr."
    shared void logWriter(Priority priority, Module|Package mod,
            String message, Throwable? except) {
        process.writeErrorLine("``priority`` (``mod``): ``message``");
        if (exists except) {
            process.writeErrorLine(except.message);
            except.printStackTrace();
        }
    }
    "Create the cache of driver objects."
    shared [Map<String, ISPDriver>, Map<String, ISPDriver>] createCache() {
        MutableMap<String, ISPDriver> cliCache = HashMap<String, ISPDriver>();
        MutableMap<String, ISPDriver> guiCache = HashMap<String, ISPDriver>();
        {String*} reserved = ["-g", "-c", "--gui", "--cli"];
        MutableMultimap<String, ISPDriver> conflicts =
		        ArrayListMultimap<String, ISPDriver>();
        void addToCache(ISPDriver* drivers) {
            for (driver in drivers) {
                MutableMap<String, ISPDriver> cache;
                if (driver.usage.graphical) {
                    cache = guiCache;
                } else {
                    cache = cliCache;
                }
                for (option in driver.usage.invocations) {
                    if (reserved.contains(option)) {
                        log.error("A driver wants to register for a reserved option '``
                            option``': claims to be ``driver.usage.shortDescription``");
                    } else if (conflicts.defines(option)) {
                        log.warn("Additional conflict for '``option``': '``
                            driver.usage.shortDescription``'");
                        conflicts.put(option, driver);
                    } else if (exists existing = cache[option]) {
                        log.warn("Invocation option conflict for '``option``' between '``
                            driver.usage.shortDescription``' and '``
	                        existing.usage.shortDescription``'");
                        conflicts.put(option, driver);
                        conflicts.put(option, existing);
                        cache.remove(option);
                    } else {
                        cache[option] = driver;
                    }
                }
            }
        }
        addToCache(*`module strategicprimer.viewer`.findServiceProviders(`ISPDriver`));
        return [cliCache, guiCache];
    }
    "Create the usage message for a particular driver."
    shared String usageMessage(IDriverUsage usage, Boolean verbose) {
        StringBuilder builder = StringBuilder();
        builder.append("Usage: ");
        String mainInvocation;
        if (exists invocationResource = `module strategicprimer.viewer`
                .resourceByPath("invocation")) {
            mainInvocation = invocationResource.textContent().trimmed;
        } else {
            mainInvocation =
		            "ceylon --cwd=. run `` `module strategicprimer.viewer`.name``";
        }
        builder.append(mainInvocation);
        if (usage.graphical) {
            builder.append(" [-g|--gui] ");
        } else {
            builder.append(" -c|--cli ");
        }
        builder.append("|".join(usage.invocations));
        for (option in usage.supportedOptions) {
            builder.append(" [``option``]");
        }
        switch (usage.paramsWanted)
        case (ParamCount.none) {}
        case (ParamCount.one) { builder.append(" ``usage.firstParamDescription``"); }
        case (ParamCount.atLeastOne) {
            builder.append(" ``usage.firstParamDescription`` [``
                usage.subsequentParamDescription`` ...]");
        }
        case (ParamCount.two) {
            builder.append(" ``usage.firstParamDescription`` ``
                usage.subsequentParamDescription``");
        }
        case (ParamCount.atLeastTwo) {
            builder.append(" ``usage.firstParamDescription`` ``
                usage.subsequentParamDescription`` [``
                usage.subsequentParamDescription`` ...]");
        }
        case (ParamCount.anyNumber) {
            builder.append(" [``usage.subsequentParamDescription`` ...]");
        }
        builder.appendNewline();
        if (verbose) {
            builder.append(usage.longDescription);
        } else {
            builder.append(usage.shortDescription);
        }
        return builder.string;
    }
    shared void handleDroppedFiles(AppEvent.OpenFilesEvent openFilesEvent) {
        if (exists topWindow = WindowList.getWindows(true, false)
                .iterable.narrow<SPFrame>().last) {
            for (file in openFilesEvent.files) {
                topWindow.acceptDroppedFile(file.toPath());
            }
        }
    }
}
class AppStarter() {
    [Map<String, ISPDriver>, Map<String, ISPDriver>] driverCache =
            appChooserState.createCache(); // TODO: Can we inline that into here?
    void startCatchingErrors(ISPDriver driver, ICLIHelper cli, SPOptions options,
            String* args) {
        try {
            driver.startDriverOnArguments(cli, options, *args);
        } catch (IncorrectUsageException except) {
            cli.println(appChooserState.usageMessage(except.correctUsage,
                options.getArgument("--verbose") == "true"));
        } catch (DriverFailedException except) {
            if (is SPFormatException cause = except.cause) {
                log.error(cause.message);
            } else if (exists cause = except.cause) {
                log.error("Driver failed:", cause);
            } else {
                log.error("Driver failed:", except);
            }
        } catch (NoSuchFileException except) {
            log.error("File ``except.file`` not found");
            log.trace("File-not-found stack trace:", except);
        } catch (FileNotFoundException except) {
            log.error("File not found: ``except.message``");
            log.trace("File-not-found stack trace:", except);
        } catch (Exception except) {
            log.error(except.message, except);
        }
    }
    Boolean includeInCLIList(ISPDriver driver) => driver.usage.includeInList(false);
    shared void startDriverOnArguments(ICLIHelper cli, SPOptions options, String* args) {
        //            log.info("Inside appStarter.startDriver()");
        variable Boolean gui = !GraphicsEnvironment.headless;
        variable SPOptionsImpl currentOptions = SPOptionsImpl(options);
        if (!currentOptions.hasOption("--gui")) {
            currentOptions.addOption("--gui", gui.string);
        }
        MutableList<String> others = ArrayList<String>();
        void startChosenDriver(ISPDriver driver, SPOptions currentOptionsTyped) {
            if (driver.usage.graphical) {
                SwingUtilities.invokeLater(defer(startCatchingErrors,
                    [driver, cli, currentOptionsTyped, *others]));
            } else {
                startCatchingErrors(driver, cli, currentOptionsTyped, *others);
            }
            // TODO: clear `others` here?
        }
        variable ISPDriver? currentDriver = null;
        for (arg in args.coalesced) {
            if (arg == "-g" || arg == "--gui") {
                log.trace("User specified either -g or --gui");
                currentOptions.addOption("--gui");
                gui = true;
            } else if (arg == "-c" || arg == "--cli") {
                log.trace("User specified either -c or --cli");
                currentOptions.addOption("--gui", "false");
                gui = false;
            } else if (arg.startsWith("--gui=")) {
                String tempString = arg.substring(6);
                log.trace("User specified --gui=``tempString``");
                value tempBool = Boolean.parse(tempString);
                if (is Boolean tempBool) {
                    currentOptions.addOption("--gui", tempString);
                    gui = tempBool;
                } else {
                    throw DriverFailedException(tempBool, "--gui=nonBoolean");
                }
            } else if (arg.startsWith("-") && arg.contains("=")) {
                {String+} broken = arg.split('='.equals, true, false);
                currentOptions.addOption(broken.first, broken.rest.reduce<String>(
                    (String partial, String element) =>
                            if (partial.empty) then element
	                            else "``partial``=``element``")
                else "");
                log.trace(
	                "User specified ``broken.first``=``broken.rest.first else ""``");
            } else if (!gui, driverCache[0].defines(arg.lowercased)) {
                log.trace("User specified app-choosing option ``arg`` while in CLI mode");
                if (exists temp = currentDriver) {
                    log.trace("Starting previously chosen CLI app.");
                    startChosenDriver(temp, currentOptions.copy());
                }
                currentDriver = driverCache[0][arg.lowercased];
            } else if (gui, driverCache[1].defines(arg.lowercased)) {
                log.trace("User specified app-choosing option ``arg`` while in GUI mode");
                if (exists temp = currentDriver) {
                    log.trace("Starting previously chosen GUI app.");
                    startChosenDriver(temp, currentOptions.copy());
                }
                currentDriver = driverCache[1][arg.lowercased];
            } else if (driverCache[0].defines(arg.lowercased)) {
                log.warn("We're in GUI mode, but CLI-only app specified");
                log.trace("User specified CLI app ``arg`` while in GUI mode");
                if (exists temp = currentDriver) {
                    log.trace("Starting previously chosen GUI app.");
                    startChosenDriver(temp, currentOptions.copy());
                }
                currentDriver = driverCache[0][arg.lowercased];
            } else if (driverCache[1].defines(arg.lowercased)) {
                log.warn("We're in CLI mode, but GUI-only app specified");
                log.trace("User specified GUI app ``arg`` while in CLI mode.");
                if (exists temp = currentDriver) {
                    log.trace("Starting previously chosen CLI app.");
                    startChosenDriver(temp, currentOptions.copy());
                }
                currentDriver = driverCache[1][arg.lowercased];
            } else if (arg.startsWith("-")) {
                log.trace("User specified non-app-choosing option ``arg``");
                currentOptions.addOption(arg);
            } else {
                log.trace("User specified non-option argument ``arg``");
                others.add(arg);
            }
        }
        log.trace("Reached the end of options");
        if (currentOptions.hasOption("--help")) { // TODO: Handle --help in startChosenDriver() instead, so it works for drivers other than the last (TODO: figure out how to ma
            if (exists currentUsage = currentDriver?.usage) {
                log.trace("Giving usage information for selected driver");
                process.writeLine(appChooserState.usageMessage(currentUsage,
                    options.getArgument("--verbose") == "true"));
            } else {
                log.trace("No driver selected, so giving choices.");
                process.writeLine("Strategic Primer assistive programs suite");
                process.writeLine(
	                "No app specified; use one of the following invocations:");
                process.writeLine();
                for (driver in driverCache[0].chain(driverCache[1])
		                .map(Entry.item).distinct) {
                    value lines = appChooserState.usageMessage(driver.usage,
                        options.getArgument("--verbose") == "true").lines;
                    String invocationExample = lines.first.replace("Usage: ", "");
                    String description =
                            lines.rest.first?.replace(".", "") else "An unknown app";
                    process.writeLine("``description``: ``invocationExample``");
                }
            }
        } else if (exists driver = currentDriver) {
            log.trace("Starting chosen app.");
            startChosenDriver(driver, currentOptions.copy());
        } else {
            log.trace("Starting app-chooser.");
            SPOptions currentOptionsTyped = currentOptions.copy();
            if (gui) {
                try {
                    SwingUtilities.invokeLater(
                        defer(shuffle(compose(SPFrame.showWindow, appChooserFrame))(),
	                        [cli, currentOptionsTyped, others]));
                } catch (DriverFailedException except) {
                    log.fatal(except.message, except);
                    SwingUtilities.invokeLater(defer(showErrorDialog, [null,
                        "Strategic Primer Assistive Programs", except.message]));
                }
            } else {
                if (exists chosenDriver = cli.chooseFromList(
                        driverCache.first.items.distinct.filter(includeInCLIList)
	                        .sequence(),
                        "CLI apps available:", "No applications available",
                        "App to start: ", true).item) {
                    startCatchingErrors(chosenDriver, cli, options, *others);
                }
            }
        }
    }
}
todo("Try to combine/rearrange things so we have as few top-level and inner classes and
      `object`s as possible")
suppressWarnings("expressionTypeNothing")
shared void run() {
    addLogWriter(appChooserState.logWriter);
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        "SP Helpers");
    System.setProperty("apple.awt.application.name", "SP Helpers");
    UIManager.setLookAndFeel(UIManager.systemLookAndFeelClassName);
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    if (process.namedArgumentPresent("trace")) {
        defaultPriority = trace;
    } else if (process.namedArgumentPresent("debug")) {
        defaultPriority = debug;
    }
    log.debug("If you can see this, debug-level log messages are enabled.");
    log.trace("If you can see this, trace-level log messages are enabled.");
    SPOptionsImpl options = SPOptionsImpl();
    if (platform.systemIsMac) {
        Application.application.setOpenFileHandler(appChooserState.handleDroppedFiles);
    }
    AppStarter appStarter = AppStarter();
    try {
        appStarter.startDriverOnArguments(CLIHelper(), options, *process.arguments);
    } catch (IncorrectUsageException except) {
        IDriverUsage usage = except.correctUsage;
        process.writeErrorLine(appChooserState.usageMessage(usage,
            options.hasOption("--verbose")));
        process.exit(1);
    } catch (IOException|DriverFailedException except) {
        log.error(except.message, except.cause);
        process.exit(2);
    }
}
SPFrame appChooserFrame(ICLIHelper cli, SPOptions options,
        {String*}|IDriverModel finalArg) {
    value tempComponent = JEditorPane();
    value font = tempComponent.font;
    assert (is Graphics2D pen = BufferedImage(1, 1, BufferedImage.typeIntRgb)
        .createGraphics());
    value context = pen.fontRenderContext;
    variable Integer width = 0;
    variable Integer height = 10;
    Boolean includeInGUIList(ISPDriver driver) => driver.usage.includeInList(true);
    value drivers = `module strategicprimer.viewer`.findServiceProviders(`ISPDriver`)
        .filter(includeInGUIList).sequence();
    for (driver in drivers) {
        value dimensions = font.getStringBounds(driver.usage.shortDescription, context);
        width = Integer.largest(width, dimensions.width.integer);
        height += dimensions.height.integer;
    }
    SPFrame frame = SPFrame("SP App Chooser", null, Dimension(width, height));
    void buttonHandler(ISPDriver target) {
        try {
            if (is IDriverModel finalArg) {
                target.startDriverOnModel(cli, options, finalArg);
            } else {
                target.startDriverOnArguments(cli, options, *finalArg);
            }
            SwingUtilities.invokeLater(() {
                frame.setVisible(false);
                frame.dispose();
            });
        } catch (IOException except) {
            log.error("I/O error prompting user for app to start", except);
            showErrorDialog(frame, "I/O error", except.message );
        } catch (DriverFailedException except) {
            if (is SPFormatException cause = except.cause) {
                showErrorDialog(frame, except.message, cause.message);
                log.error(cause.message);
            } else if (exists cause = except.cause) {
                showErrorDialog(frame, except.message, cause.message);
                log.error("Driver failed:", cause);
            } else {
                showErrorDialog(frame, except.message, except.message);
                log.error("Driver failed:", except);
            }
        } catch (Exception except) {
            showErrorDialog(frame, except.message, except.message);
            log.error(except.message, except);
        }
    }
    JPanel buttonPanel = JPanel(GridLayout(0, 1));
    for (driver in drivers) {
        buttonPanel.add(listenedButton(driver.usage.shortDescription,
                    (evt) => buttonHandler(driver)));
    }
    frame.contentPane = BorderedPanel.verticalPanel(
        JLabel("Please choose one of the applications below"),
        JScrollPane(buttonPanel), null);
    frame.pack();
    frame.jMenuBar = UtilityMenu(frame);
    frame.addWindowListener(WindowCloseListener(silentListener(frame.dispose)));
    return frame;
}

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
    Dimension
}
import java.io {
    IOException
}
import java.lang {
    System
}

import javax.swing {
    UIManager,
    SwingUtilities,
    JPanel,
    JScrollPane,
    JLabel
}

import lovelace.util.jvm {
    showErrorDialog,
    BorderedPanel,
    platform,
	listenedButton
}

import strategicprimer.viewer.drivers.advancement {
    advancementCLI,
    advancementGUI
}
import strategicprimer.viewer.drivers.exploration {
    explorationGUI,
    explorationCLI
}
import strategicprimer.viewer.drivers.map_viewer {
    viewerGUI,
    drawHelperComparator
}
import strategicprimer.viewer.drivers.mining {
    miningCLI
}
import strategicprimer.drivers.utility.subset {
    subsetCLI,
    subsetGUI
}
import strategicprimer.viewer.drivers.worker_mgmt {
    workerGUI,
    strategyExportCLI
}
import strategicprimer.drivers.common {
    IDriverModel,
    ISPDriver,
    IDriverUsage,
    SPOptions,
    ParamCount,
    DriverFailedException,
    IncorrectUsageException,
    DriverUsage,
    SPOptionsImpl
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import com.apple.eawt {
    AppEvent,
    Application
}
import com.pump.window {
    WindowList
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import strategicprimer.drivers.generators {
    townGeneratingCLI,
    statGeneratingCLI,
	populationGeneratingCLI
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    UtilityMenu
}
import strategicprimer.drivers.utility {
    mapCheckerCLI,
    readerComparator,
    mapCheckerGUI,
    duplicateFixtureRemoverCLI,
	randomMovementCLI,
	mapTradeCLI,
	workerPrintCLI
}
import strategicprimer.model.xmlio {
    SPFormatException
}
import strategicprimer.viewer.drivers.query {
	queryCLI,
	trappingCLI
}
import lovelace.util.common {
	todo
}
import com.vasileff.ceylon.structures {
	MutableMultimap,
	ArrayListMultimap
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
		MutableMultimap<String, ISPDriver> conflicts = ArrayListMultimap<String, ISPDriver>();
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
						log.error("A driver wants to register for a reserved option '``option``': claims to be ``
							driver.usage.shortDescription``");
					} else if (conflicts.defines(option)) {
						log.warn("Additional conflict for '``option``': '``driver.usage.shortDescription``'");
						conflicts.put(option, driver);
					} else if (exists existing = cache[option]) {
						log.warn("Invocation option conflict for '``option``' between '``
							driver.usage.shortDescription``' and '``existing.usage.shortDescription``'");
						conflicts.put(option, driver);
						conflicts.put(option, existing);
						cache.remove(option);
					} else {
						cache[option] = driver;
					}
				}
			}
		}
		addToCache(
			reportCLI,
			viewerGUI,
			advancementCLI,
			advancementGUI,
			strategyExportCLI,
			workerGUI,
			explorationCLI,
			explorationGUI,
			readerComparator,
			drawHelperComparator,
			mapCheckerCLI,
			mapCheckerGUI,
			subsetCLI,
			subsetGUI,
			// FIXME: Write GUI equivalent of query CLI
			queryCLI,
			echoDriver,
			// FIXME: Write GUI for the duplicate fixture remover
			duplicateFixtureRemoverCLI,
			// FIXME: Write trapping (and hunting, etc.) GUI
			trappingCLI,
			// FIXME: Write stat-generating/stat-entering GUI
			statGeneratingCLI,
			// FIXME: Write GUI for map-expanding driver
			expansionDriver,
			// TODO: Write GUI equivalent of Map Populator Driver
			mapPopulatorDriver,
			resourceAddingCLI, resourceAddingGUI,
			tabularReportCLI, tabularReportGUI,
			// TODO: Write GUI to allow user to visually explore a mine
			miningCLI,
			// TODO: Write GUI to allow user to generate or enter town contents
			townGeneratingCLI,
			randomMovementCLI,
			mapTradeCLI,
			populationGeneratingCLI,
			workerPrintCLI
		);
	    return [cliCache, guiCache];
	}
	"Create the usage message for a particular driver."
	shared String usageMessage(IDriverUsage usage, Boolean verbose) {
	    StringBuilder builder = StringBuilder();
	    builder.append("Usage: ");
	    String mainInvocation;
	    if (exists invocationResource = `module strategicprimer.viewer`.resourceByPath("invocation")) {
	        mainInvocation = invocationResource.textContent().trimmed;
	    } else {
	        mainInvocation = "ceylon --cwd=. run `` `module strategicprimer.viewer`.name```";
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
todo("Try to combine/rearrange things so we have as few top-level and inner classes and `object`s as possible")
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
    [Map<String, ISPDriver>, Map<String, ISPDriver>] driverCache = appChooserState.createCache();
    if (platform.systemIsMac) {
        Application.application.setOpenFileHandler(appChooserState.handleDroppedFiles);
    }
    object appStarter satisfies ISPDriver {
        shared actual IDriverUsage usage = DriverUsage(true, ["-p", "--app-starter"],
            ParamCount.anyNumber, "App Chooser",
            "Let the user choose an app to start, or handle options.");
        void startCatchingErrors(ISPDriver driver, ICLIHelper cli, SPOptions options, String* args) {
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
            } catch (Exception except) {
                log.error(except.message, except);
            }
        }
        shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
                String* args) {
//            log.info("Inside appStarter.startDriver()");
            variable Boolean gui = !GraphicsEnvironment.headless;
            variable SPOptionsImpl currentOptions = SPOptionsImpl(options);
            if (!currentOptions.hasOption("--gui")) {
                currentOptions.addOption("--gui", gui.string);
            }
            MutableList<String> others = ArrayList<String>();
            void startChosenDriver(ISPDriver driver, SPOptions currentOptionsTyped) {
                if (driver.usage.graphical) {
                    SwingUtilities.invokeLater(() =>
                        startCatchingErrors(driver, cli, currentOptionsTyped, *others));
                } else {
                    startCatchingErrors(driver, cli, currentOptionsTyped, *others);
                }
                // TODO: clear `others` here?
            }
            variable ISPDriver? currentDriver = null;
            for (arg in args.coalesced) {
                if (arg == "-g" || arg == "--gui") {
                    currentOptions.addOption("--gui");
                    gui = true;
                } else if (arg == "-c" || arg == "--cli") {
                    currentOptions.addOption("--gui", "false");
                    gui = false;
                } else if (arg.startsWith("--gui=")) {
                    String tempString = arg.substring(6);
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
                        if (partial.empty) then element else "``partial``=``element``")
                        else "");
                } else if (!gui, driverCache[0].defines(arg.lowercased)) {
                    if (exists temp = currentDriver) {
                        startChosenDriver(temp, currentOptions.copy());
                    }
                    currentDriver = driverCache[0][arg.lowercased];
                } else if (gui, driverCache[1].defines(arg.lowercased)) {
                    if (exists temp = currentDriver) {
                        startChosenDriver(temp, currentOptions.copy());
                    }
                    currentDriver = driverCache[1][arg.lowercased];
                } else if (driverCache[0].defines(arg.lowercased)) {
                    log.warn("We're in GUI mode, but CLI-only app specified");
                    if (exists temp = currentDriver) {
                        startChosenDriver(temp, currentOptions.copy());
                    }
                    currentDriver = driverCache[0][arg.lowercased];
                } else if (driverCache[1].defines(arg.lowercased)) {
                    log.warn("We're in CLI mode, but GUI-only app specified");
                    if (exists temp = currentDriver) {
                        startChosenDriver(temp, currentOptions.copy());
                    }
                    currentDriver = driverCache[1][arg.lowercased];
                } else if (arg.startsWith("-")) {
                    currentOptions.addOption(arg);
                } else {
                    others.add(arg);
                }
            }
            if (options.hasOption("--help")) {
                IDriverUsage tempUsage = currentDriver?.usage else usage;
                process.writeLine(appChooserState.usageMessage(tempUsage,
                        options.getArgument("--verbose") == "true"));
            } else if (exists driver = currentDriver) {
                startChosenDriver(driver, currentOptions.copy());
            } else {
                SPOptions currentOptionsTyped = currentOptions.copy();
                if (gui) {
                    try {
                        SwingUtilities.invokeLater(() => appChooserFrame(cli,
                            currentOptionsTyped, others).setVisible(true));
                    } catch (DriverFailedException except) {
                        log.fatal(except.message, except);
                        SwingUtilities.invokeLater(() => showErrorDialog(null,
                            "Strategic Primer Assistive Programs", except.message));
                    }
                } else {
                    ISPDriver[] driversList = driverCache.first.items.distinct.sequence();
                    value choice = cli.chooseFromList(driversList,
                        "CLI apps available:", "No applications available",
                        "App to start: ", true);
                    if (exists chosenDriver = choice.item) {
                        startCatchingErrors(chosenDriver, cli, options, *others);
                    }
                }
            }
        }

        shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
                IDriverModel driverModel) {
            if (GraphicsEnvironment.headless || options.getArgument("--gui") == "false") {
                ISPDriver[] cliDrivers = driverCache.first.items.distinct.sequence();
                try {
                    if (exists driver = cli.chooseFromList(
                            cliDrivers, "CLI apps available:",
                            "No applications available", "App to start: ", true).item) {
                        driver.startDriverOnModel(cli, options, driverModel);
                    }
                } catch (IOException except) {
                    log.error("I/O error prompting user for app to start", except);
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
                } catch (Exception except) {
                    log.error(except.message, except);
                }
            } else {
                SwingUtilities.invokeLater( // TODO: catch errors (combine with the above)
                    () => appChooserFrame(cli, options, driverModel).setVisible(true));
            }
        }
    }
    try {
        appStarter.startDriverOnArgumentsNoCLI(options, *process.arguments);
    } catch (IncorrectUsageException except) {
        IDriverUsage usage = except.correctUsage;
        process.writeErrorLine(appChooserState.usageMessage(usage, options.hasOption("--verbose")));
        process.exit(1);
    } catch (IOException|DriverFailedException except) {
        log.error(except.message, except.cause);
        process.exit(2);
    }
}
suppressWarnings("expressionTypeNothing")
SPFrame appChooserFrame(ICLIHelper cli, SPOptions options,
        {String*}|IDriverModel finalArg) {
	SPFrame frame = SPFrame("SP App Chooser", null, Dimension(220, 110));
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
    buttonPanel.add(listenedButton("Map Viewer", (evt) => buttonHandler(viewerGUI)));
    buttonPanel.add(listenedButton("Worker Skill Advancement", (evt) => buttonHandler(advancementGUI)));
    buttonPanel.add(listenedButton("Unit Orders and Worker Management", (evt) => buttonHandler(workerGUI)));
    buttonPanel.add(listenedButton("Exploration", (evt) => buttonHandler(explorationGUI)));
    frame.contentPane = BorderedPanel.verticalPanel(
        JLabel("Please choose one of the applications below"),
        JScrollPane(buttonPanel), null);
    frame.pack();
    MenuBroker menuHandler = MenuBroker();
    menuHandler.register((event) => frame.dispose(), "close");
    menuHandler.register((event) => aboutDialog(frame, frame.windowName).setVisible(true), "about");
    menuHandler.register((event) => process.exit(0), "quit");
    frame.jMenuBar = UtilityMenu(frame);
    return frame;
}

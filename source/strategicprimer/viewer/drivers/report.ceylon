import ceylon.file {
    parsePath,
    Nil,
    File,
    Path,
    Writer
}

import java.io {
    IOException,
    IOError
}

import strategicprimer.drivers.common {
    IMultiMapModel,
    IDriverModel,
    ParamCount,
    IDriverUsage,
    SPOptions,
    DriverUsage,
    DriverFailedException,
    GUIDriver,
    ReadOnlyDriver,
    ModelDriverFactory,
    DriverFactory,
    ModelDriver,
    SimpleMultiMapModel,
    GUIDriverFactory,
    SimpleDriverModel,
    MapChangeListener
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    Player,
    IMapNG,
    IMutableMapNG
}
import strategicprimer.report {
    reportGenerator,
    tabularReportGenerator
}
import javax.swing {
    JTabbedPane
}
import java.awt {
    Dimension,
    Component
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    WindowCloseListener,
    SPFileChooser,
    SPMenu
}
import ceylon.collection {
    HashMap,
    MutableMap,
    MutableSet,
    HashSet
}
import lovelace.util.common {
    matchingValue,
    silentListener,
    entryMap,
    PathWrapper
}
import lovelace.util.jvm {
    FileChooser
}
import org.takes.facets.fork {
    Fork,
    FkRegex,
    TkFork
}
import org.takes.rs {
    RsHtml,
    RsWithType,
    RsWithHeader,
    RsText
}
import org.takes.tk {
    TkRedirect
}
import org.takes.http {
    FtBasic,
    Exit
}

"An object to help us present files with only as much of their paths as
 necessary to uniquely identify them, without their shared prefix."
object suffixHelper {
    "Get the last [[count]] path elements in [[the given path|file]]."
    String suffix(Path file, Integer count) {
        Integer start;
        if (count >= file.elementPaths.size) {
            start = 0;
        } else {
            start = file.elementPaths.size - count;
        }
        Integer end;
        if (file.elementPaths.size == 0) {
            end = 1;
        } else {
            end = file.elementPaths.size;
        }
        return "/".join(file.elementPaths.sublist(start, end));
    }

    "Divide the given [[filename|file]] into prefix and suffix, returning the
     suffix, such that the prefix is shared with all files in [[all]], but is
     otherwise as long as possible."
    shared String shortestSuffix({Path*} all, Path file) {
        Integer longestPath = Integer.max(all.map(compose(Sequential<Path>.size,
            Path.elementPaths))) else 1;
        MutableSet<String> localCache = HashSet<String>();
        for (num in 1..longestPath) {
            for (key in all) {
                String item = suffix(key, num);
                if (localCache.contains(item)) {
                    break;
                } else {
                    localCache.add(item);
                }
            } else {
                return suffix(file, num);
            }
        }
        return file.string;
    }
}

"A factory for a driver to produce a report of the contents of a map."
service(`interface DriverFactory`)
shared class ReportCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["create-report"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Report Generator";
        longDescription = "Produce HTML report of the contents of a map";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [
            ("\\" == operatingSystem.fileSeparator) then
            "--out=C:\\path\\to\\output.html"
            else "--out=/path/to/output.html",
            "--player=NN", "--current-turn=NN", "--serve[=8080]"
        ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (options.hasOption("--serve")) {
            return ReportServingCLI(options, model);
        } else {
            return ReportCLI(options, model);
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);
}

"""A driver to "serve" a report on the contents of a map on an embedded HTTP server."""
class ReportServingCLI(SPOptions options, model) satisfies ReadOnlyDriver {
    shared actual IDriverModel model;
    void serveReports(Integer port, Player? currentPlayer) {
        MutableMap<Path, String> cache = HashMap<Path, String>();
        if (is IMultiMapModel model) {
            for (map->[file, _] in model.allMaps) {
                if (exists file, !cache.defines(parsePath(file.filename))) {
                    cache[parsePath(file.filename)] = reportGenerator.createReport(map,
                        currentPlayer else map.currentPlayer);
                }
            }
        } else if (exists file = model.mapFile) {
            cache[parsePath(file.filename)] = reportGenerator.createReport(model.map,
                currentPlayer else model.map.currentPlayer);
        }
        if (cache.empty) {
            return;
        } else {
            value localCache = cache.map(
                        (file->report) => suffixHelper.shortestSuffix(cache.keys,
                    file.absolutePath)->report);
            {Fork*} endpoints = localCache.map((file->report) => FkRegex("/``file``",
                RsHtml(report)));
            Fork rootHandler;
            if (localCache.size == 1) {
                assert (exists soleFile = localCache.first?.key);
                rootHandler = FkRegex("/", TkRedirect("/" + soleFile));
            } else {
                StringBuilder builder = StringBuilder();
                builder.append(
                    """<!DOCTYPE html>
                       <html>
                           <head>
                               <title>Strategic Primer Reports</title>
                           </head>
                           <body>
                               <h1>Strategic Primer Reports</h1>
                               <ul>
                       """);
                for (file->report in localCache) {
                    builder.appendAll(["            <li><a href=\"", file, "\">", file,
                        "</a></li>"]);
                    builder.appendNewline();
                }
                builder.append("""         </ul>
                                      </body>
                                  </html>""");
                rootHandler = FkRegex("/", RsHtml(builder.string));
            }
            log.info("About to start serving on port ``port``");
            FtBasic(TkFork(rootHandler, *endpoints), port).start(Exit.never);
        }
    }

    shared actual void startDriver() {
        String portArgument = options.getArgument("--serve");
        value tempPort = Integer.parse(portArgument);
        Integer port;
        if (is Integer tempPort) {
            port = tempPort;
        } else {
            if (portArgument != "true") {
                log.warn("Port must be a number");
                log.trace("Stack trace of port parse failure", tempPort);
            }
            port = 8080;
        }
        value playerNum = Integer.parse(options.getArgument("--player"));
        Player? player;
        if (is Integer playerNum) {
            player = model.map.players.getPlayer(playerNum);
        } else {
            player = null;
        }
        serveReports(port, player);
    }
}

"A driver to produce a report of the contents of a map."
shared class ReportCLI(SPOptions options, model) satisfies ReadOnlyDriver {
    shared actual IDriverModel model;

    void writeReport(Path? filename, IMapNG map) {
        if (exists filename) {
            Player player;
            if (options.hasOption("--player")) {
                value playerNum = Integer.parse(options.getArgument("--player"));
                if (is Integer playerNum) {
                    player = map.players.getPlayer(playerNum);
                } else {
                    log.warn("Non-numeric player", playerNum);
                    player = map.currentPlayer;
                }
            } else {
                player = map.currentPlayer;
            }
            String outString;
            Path outPath;
            if (options.hasOption("--out")) {
                outString = options.getArgument("--out");
                outPath = parsePath(outString);
            } else {
                outString = "``filename.elements.last else filename``.report.html";
                outPath = filename.siblingPath(outString);
            }
            if (is Nil loc = outPath.resource) {
                value file = loc.createFile();
                try (writer = file.Overwriter()) {
                    writer.write(reportGenerator.createReport(map, player));
                }
            }
        } else {
            log.error("Asked to make report from map with no filename");
        }
    }

    shared actual void startDriver() {
        if (is IMultiMapModel model) {
            for (map->[file, _] in model.allMaps) {
                Path? wrapped =
                        if (exists file) then parsePath(file.filename) else null;
                writeReport(wrapped, map);
            }
        } else {
            writeReport(
                if (exists file = model.mapFile)
                    then parsePath(file.filename) else null,
                model.map);
        }
    }
}

"A factory for a driver to show tabular reports of the contents of a player's map in a
 GUI."
service(`interface DriverFactory`)
shared class TabularReportGUIFactory() satisfies GUIDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(true, ["tabular-report"],
        ParamCount.one, "Tabular Report Viewer",
        "Show the contents of a map in tabular form", false, true);
    "Ask the user to choose a file."

    shared actual {PathWrapper*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }

    shared actual GUIDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => TabularReportGUI(cli, options, model);

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleDriverModel(map, path);
}

"A driver to show tabular reports of the contents of a player's map in a GUI."
shared class TabularReportGUI(ICLIHelper cli, SPOptions options, model)
        satisfies GUIDriver {
    shared actual IDriverModel model;

    shared actual void startDriver() {
        SPFrame window = SPFrame("Tabular Report", this, Dimension(640, 480));
        JTabbedPane frame = JTabbedPane(JTabbedPane.top, JTabbedPane.scrollTabLayout);
        object listener satisfies MapChangeListener {
            shared actual void mapChanged() {
                frame.removeAll();
                tabularReportGenerator.createGUITabularReports(
                    // can't use a method reference here: JTabbedPane.addTab is overloaded
                    (String str, Component comp) => frame.addTab(str, comp), model.map);
            }
            shared actual void mapMetadataChanged() {}
        }
        listener.mapChanged();
        model.addMapChangeListener(listener);
        window.add(frame);
        window.jMenuBar = SPMenu.forWindowContaining(frame,
            SPMenu.createFileMenu(IOHandler(this).actionPerformed, this),
            SPMenu.disabledMenu(SPMenu.createMapMenu(noop, this)),
            SPMenu.disabledMenu(SPMenu.createViewMenu(noop, this)));
        window.addWindowListener(WindowCloseListener(silentListener(window.dispose)));
        window.showWindow();
    }

    "Ask the user to choose a file."
    shared actual {PathWrapper*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }

    shared actual void open(IMutableMapNG map, PathWrapper? path) =>
            model.setMap(map, path);
}

"A factory for a driver to produce tabular (CSV) reports of the contents of a player's
 map."
service(`interface DriverFactory`)
shared class TabularReportCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["tabular-report"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Tabular Report Generator";
        longDescription = "Produce CSV reports of the contents of a map.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = ["--serve[=8080]"];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (options.hasOption("--serve")) {
            return TabularReportServingCLI(options, model);
        } else {
            return TabularReportCLI(cli, options, model);
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);
}

class TabularReportServingCLI(SPOptions options, model) satisfies ReadOnlyDriver {
    shared actual IDriverModel model;

    Item->Key reverseEntry<Key, Item>(Key->Item entry)
            given Key satisfies Object given Item satisfies Object =>
            entry.item->entry.key;

    void serveReports(Integer port) {
        Map<Path, IMapNG> mapping;
        if (is IMultiMapModel model) {
            mapping = map(model.allMaps.coalesced
                .map(entryMap(identity<IMutableMapNG>, Tuple<PathWrapper?|Boolean,
            PathWrapper?, [Boolean]>.first)).map(Entry.coalesced).coalesced
                .map(entryMap(identity<IMutableMapNG>,
                compose(parsePath, PathWrapper.filename)))
                .map(reverseEntry));
        } else if (exists path = model.mapFile) {
            mapping = map { parsePath(path.filename)->model.map };
        } else {
            mapping = map { parsePath("unknown.xml")->model.map };
        }

        MutableMap<[String, String], StringBuilder> builders =
                HashMap<[String, String], StringBuilder>();

        Anything(String)(String) filenameFunction(Path base) {
            String baseName = suffixHelper.shortestSuffix(mapping.keys, base);
            return (String tableName) {
                if (exists writer = builders.get([baseName, tableName])) {
                    return writer.append;
                } else {
                    StringBuilder writer = StringBuilder();
                    builders[[baseName, tableName]] = writer;
                    return writer.append;
                }
            };
        }

        void createReports(IMapNG map, Path? mapFile) {
            if (exists mapFile) {
                try {
                    tabularReportGenerator.createTabularReports(map,
                        filenameFunction(mapFile));
                } catch (IOException|IOError except) {
                    throw DriverFailedException(except);
                }
            } else {
                log.error("Asked to create reports from map with no filename");
            }
        }

        if (is IMultiMapModel model) {
            for (map->[file, _] in model.allMaps) {
                createReports(map, parsePath(file?.filename else "unknown.xml"));
            }
        } else {
            createReports(model.map,
                parsePath(model.mapFile?.filename else "unknown.xml"));
        }

        {Fork*} endpoints = builders
            .map(([file, table]->builder) => FkRegex("/``file``.``table``.csv",
                RsWithType(RsWithHeader(RsText(builder.string),
                        "Content-Disposition", "attachment; filename=\"``table``.csv\""),
                    "text/csv")));
        String tocHtml(String path) {
            StringBuilder builder = StringBuilder();
            builder.append(
                "<!DOCTYPE html>
                 <html>
                     <head>
                         <title>Tabular reports for ``path``</title>
                     </head>
                     <body>
                         <h1>Tabular reports for ``path``</h1>
                         <ul>
                 ");
            for ([mapFile, table] in builders.keys.filter(matchingValue(path,
                    Tuple<String, String, [String]>.first))) {
                builder.appendAll(["            <li><a href=\"/", mapFile, ".", table,
                    ".csv\">", table, ".csv</a></li>"]);
                builder.appendNewline();
            }
            builder.append(
                """        </ul>
                       </body>
                   </html>""");
            return builder.string;
        }
        {Fork*} tocs = mapping.keys.map(curry(suffixHelper.shortestSuffix)(mapping.keys))
            .flatMap(
                (path) => [FkRegex("/``path``", RsHtml(tocHtml(path))),
                    FkRegex("/``path``/", RsHtml(tocHtml(path)))]);

        StringBuilder rootDocument = StringBuilder();
        rootDocument.append(
            """<!DOCTYPE html>
               <html>
                   <head>
                       <title>Strategic Primer Tabular Reports</title>
                   </head>
                   <body>
                       <h1>Strategic Primer Tabular Reports</h1>
                       <ul>
               """);
        for (file in mapping.keys.map(curry(suffixHelper.shortestSuffix)(mapping.keys))) {
            rootDocument.appendAll(["            <li><a href=\"/", file, "\">", file,
                "</a></li>"]);
            rootDocument.appendNewline();
        }
        rootDocument.append(
            """        </ul>
                   </body>
               </html>""");

        log.info("About to start serving on port ``port``");
        FtBasic(
            TkFork(FkRegex("/", RsHtml(rootDocument.string)), FkRegex("/index.html",
                RsHtml(rootDocument.string)), *tocs.chain(endpoints).sequence()), port)
            .start(Exit.never);
    }

    shared actual void startDriver() {
        String portArgument = options.getArgument("--serve");
        value tempPort = Integer.parse(portArgument);
        Integer port;
        if (is Integer tempPort) {
            port = tempPort;
        } else {
            if (portArgument != "true") {
                log.warn("Port must be a number");
                log.trace("Stack trace of port parse failure", tempPort);
            }
            port = 8080;
        }
        serveReports(port);
    }
}

"A driver to produce tabular (CSV) reports of the contents of a player's map."
shared class TabularReportCLI(ICLIHelper cli, SPOptions options, model)
        satisfies ReadOnlyDriver {
    shared actual IDriverModel model;

    MutableMap<String,Writer> writers = HashMap<String,Writer>();
    Anything(String)(String) filenameFunction(Path base) {
        assert (exists baseName = base.elements.terminal(1).first);
        Anything(String) retval(String tableName) {
            if (exists writer = writers.get("``baseName``.``tableName``.csv")) {
                return writer.write;
            } else {
                File file;
                switch (temp = base.siblingPath("``baseName``.``tableName``.csv")
                    .resource)
                case (is File) {
                    file = temp;
                }
                case (is Nil) {
                    file = temp.createFile();
                }
                else {
                    throw IOException(
                        "``base``.``tableName``.csv exists but is not a file");
                }
                value writer = file.Overwriter();
                writers["``baseName``.``tableName``.csv"] = writer;
                return writer.write;
            }
        }
        return retval;
    }

    void createReports(IMapNG map, Path? mapFile) {
        if (exists mapFile) {
            try {
                tabularReportGenerator.createTabularReports(map,
                    filenameFunction(parsePath(mapFile.string)));
            } catch (IOException|IOError except) {
                throw DriverFailedException(except);
            }
        } else {
            log.error("Asked to create reports from map with no filename");
        }
    }

    shared actual void startDriver() {
        if (is IMultiMapModel model) {
            for (map->[file, _] in model.allMaps) {
                Path? wrapped =
                        if (exists file) then parsePath(file.filename) else null;
                createReports(map, wrapped);
            }
        } else {
            createReports(model.map,
                if (exists file = model.mapFile)
                    then parsePath(file.filename) else null);
        }
        writers.items.each(shuffle(Writer.close)());
    }
}

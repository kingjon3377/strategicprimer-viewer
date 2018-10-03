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
    SimpleDriverModel
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
    UtilityMenu,
    WindowCloseListener
}
import ceylon.collection {
    HashMap,
    MutableMap,
    MutableSet,
    HashSet
}
import ceylon.io {
    SocketAddress
}
import ceylon.http.server {
    newServer,
    AsynchronousEndpoint,
    Endpoint,
    Request,
    Response,
    startsWith,
    matchEquals=equals,
    isRoot
}
import ceylon.http.server.endpoints {
    redirect
}
import ceylon.http.common {
    get,
    Header
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
object suffixHelper {
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
        invocations = ["-m", "--report"];
        paramsWanted = ParamCount.one;
        shortDescription = "Report Generator";
        longDescription = "Produce HTML report of the contents of a map";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptionsTemp = [
            ("\\" == operatingSystem.fileSeparator) then
            "--out=C:\\path\\to\\output.html"
            else "--out=/path/to/output.html",
            "--player=NN", "--current-turn=NN", "--serve[=8080]"
        ];
    };
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => ReportCLI(cli, options, model);

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);

}
"A driver to produce a report of the contents of a map."
// TODO: Split 'serve' and non-'serve' into separate classes, and let the factory choose between them
shared class ReportCLI(ICLIHelper cli, SPOptions options,
        IDriverModel model) satisfies ReadOnlyDriver {
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
            {Endpoint*} endpoints = localCache.map((file->report) =>
                Endpoint {
                    path = startsWith("/``file``");
                    service(Request request, Response response) =>
                            response.writeString(report);
                });
            Endpoint|AsynchronousEndpoint rootHandler;
            if (localCache.size == 1) {
                assert (exists soleFile = localCache.first?.key);
                rootHandler = AsynchronousEndpoint {
                    path = isRoot();
                    acceptMethod = [ get ];
                    service = redirect("/" + soleFile);
                };
            } else {
                rootHandler = Endpoint {
                    path = isRoot();
                    void service(Request request, Response response) {
                        response.writeString(
                            "<!DOCTYPE html>
                             <html>
                                 <head>
                                     <title>Strategic Primer Reports</title>
                                 </head>
                                 <body>
                                     <h1>Strategic Primer Reports</h1>
                                     <ul>
                             ");
                        for (file->report in localCache) {
                            response.writeString(
                                "            <li><a href=\"/``file``\">``file``</a></li>");
                        }
                        response.writeString("        </ul>
                                                  </body>
                                              </html>");
                    }
                };
            }
            log.info("About to start serving on port ``port``");
            newServer {
                rootHandler, *endpoints
            }.start(SocketAddress("127.0.0.1", port));
        }
    }
    void writeReport(Path? filename, IMapNG map, SPOptions options) {
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
        if (options.hasOption("--serve")) {
            value tempPort = Integer.parse(options.getArgument("--serve"));
            Integer port;
            if (is Integer tempPort) {
                port = tempPort;
            } else {
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
        } else {
            if (is IMultiMapModel model) {
                for (map->[file, _] in model.allMaps) {
                    Path? wrapped =
                            if (exists file) then parsePath(file.filename) else null;
                    writeReport(wrapped, map, options);
                }
            } else {
                writeReport(
                    if (exists file = model.mapFile)
                        then parsePath(file.filename) else null,
                    model.map, options);
            }
        }
    }
}
"A factory for a driver to show tabular reports of the contents of a player's map in a
 GUI."
service(`interface DriverFactory`)
shared class TabularReportGUIFactory() satisfies GUIDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(true, ["-b", "--tabular"],
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
shared class TabularReportGUI(ICLIHelper cli, SPOptions options,
        IDriverModel model) satisfies GUIDriver {
    shared actual void startDriver() {
        SPFrame window = SPFrame("Tabular Report", model.mapFile, Dimension(640, 480));
        JTabbedPane frame = JTabbedPane(JTabbedPane.top, JTabbedPane.scrollTabLayout);
        tabularReportGenerator.createGUITabularReports(
            // can't use a method reference here because JTabbedPane.addTab is overloaded
            (String str, Component comp) => frame.addTab(str, comp), model.map);
        window.add(frame);
        window.jMenuBar = UtilityMenu(window);
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
}
"A factory for a driver to produce tabular (CSV) reports of the contents of a player's
 map."
service(`interface DriverFactory`)
shared class TabularReportCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-b", "--tabular"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Tabular Report Generator";
        longDescription = "Produce CSV reports of the contents of a map.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptionsTemp = ["--serve[=8080]"];
    };
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => TabularReportCLI(cli, options, model);

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);
}
"A driver to produce tabular (CSV) reports of the contents of a player's map."
// TODO: Split 'serve' and non-'serve' into separate classes, and let the factory choose between them
shared class TabularReportCLI(ICLIHelper cli, SPOptions options,
        IDriverModel model) satisfies ReadOnlyDriver {
    MutableMap<String,Writer> writers = HashMap<String,Writer>();
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
            createReports(model.map, parsePath(model.mapFile?.filename else "unknown.xml"));
        }
        {Endpoint*} endpoints = builders.map(([file, table]->builder) =>
            Endpoint {
            path = matchEquals("/``file``.``table``.csv");
            void service(Request request, Response response) {
                response.addHeader(Header("Content-Disposition",
                    "attachment; filename=\"``table``.csv\""));
                response.writeString(builder.string);
            }
        });
        {Endpoint*} tocs = mapping.keys
            .map(curry(suffixHelper.shortestSuffix)(mapping.keys)).map(
                    (path) => Endpoint {
                        path = matchEquals("/``path``").or(matchEquals("/``path``/"));
                        void service(Request request, Response response) {
                            response.writeString(
                                "<!DOCTYPE html>
                                 <html>
                                     <head>
                                         <title>Tabular reports for ``path``</title>
                                     </head>
                                     <body>
                                         <h1>Tabular reports for ``path``</h1>
                                         <ul>
                                 ");
                            for ([mapFile, table] in builders.keys
                                    .filter(matchingValue(path,
                                        Tuple<String, String, String[]>.first))) {
                                response.writeString("            <li><a href=\"/``mapFile
                                        ``.``table``.csv\">``table``.csv</a></li>\n");
                            }
                            response.writeString("        </ul>
                                                      </body>
                                                  </html>");
                        }
                    });
        Endpoint rootHandler = Endpoint {
            path = isRoot();
            void service(Request request, Response response) {
                response.writeString(
                    "<!DOCTYPE html>
                     <html>
                         <head>
                             <title>Strategic Primer Tabular Reports</title>
                         </head>
                         <body>
                             <h1>Strategic Primer Tabular Reports</h1>
                             <ul>
                     ");
                for (file in mapping.keys
                        .map(curry(suffixHelper.shortestSuffix)(mapping.keys))) {
                    response.writeString(
                        "            <li><a href=\"/``file``\">``file``</a></li>");
                }
                response.writeString(
                    "        </ul>
                         </body>
                     </html>");
            }
        };
        log.info("About to start serving on port ``port``");
        newServer {
            rootHandler, *endpoints.chain(tocs)
        }.start(SocketAddress("127.0.0.1", port));
    }
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
        if (options.hasOption("--serve")) {
            value tempPort = Integer.parse(options.getArgument("--serve"));
            Integer port;
            if (is Integer tempPort) {
                port = tempPort;
            } else {
                port = 8080;
            }
            serveReports(port);
        } else {
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
            for (writer in writers.items) {
                writer.close();
            }
        }
    }
}

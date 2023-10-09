package drivers;

import java.util.List;
import java.util.Optional;

import java.util.stream.Stream;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import org.javatuples.Pair;

import java.nio.file.Path;

import java.io.IOException;

import drivers.common.IMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.SPOptions;
import drivers.common.DriverFailedException;
import drivers.common.ReadOnlyDriver;

import drivers.common.cli.ICLIHelper;

import common.map.Player;
import common.map.IMapNG;

import report.ReportGenerator;

import java.util.HashMap;
import java.util.Map;

import org.takes.facets.fork.Fork;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;

import org.takes.rs.RsHtml;

import org.takes.tk.TkRedirect;

import org.takes.http.FtBasic;
import org.takes.http.Exit;


/**
 * A driver to "serve" a report on the contents of a map on an embedded HTTP server.
 */
/* package */ class ReportServingCLI implements ReadOnlyDriver {
    public ReportServingCLI(final SPOptions options, final IDriverModel model, final ICLIHelper cli) {
        this.options = options;
        this.model = model;
        this.cli = cli;
    }

    private final ICLIHelper cli;
    private final SPOptions options;
    private final IDriverModel model;

    @Override
    public SPOptions getOptions() {
        return options;
    }

    @Override
    public IDriverModel getModel() {
        return model;
    }

    private void serveReports(final int port, final @Nullable Player currentPlayer) throws DriverFailedException {
        final Map<Path, String> cache = new HashMap<>();
        if (model instanceof final IMultiMapModel mmm) { // TODO: Also require there to be 1+ sub-maps w/ filenames for this case
            for (final IMapNG map : mmm.getAllMaps()) {
                final Path file = map.getFilename();
                if (file == null) {
                    continue;
                }
                if (!cache.containsKey(file)) {
                    try {
                        cache.put(file, ReportGenerator.createReport(map, cli,
                                Optional.ofNullable(currentPlayer).orElse(map.getCurrentPlayer())));
                    } catch (final IOException except) {
                        throw new DriverFailedException(except, "I/O error while creating report(s)");
                    }
                }
            }
        } else if (model.getMap().getFilename() != null) {
            try {
                cache.put(model.getMap().getFilename(), ReportGenerator.createReport(model.getMap(), cli,
                        Optional.ofNullable(currentPlayer).orElse(model.getMap().getCurrentPlayer())));
            } catch (final IOException except) {
                throw new DriverFailedException(except, "I/O error while creating report(s)");
            }
        }
        if (cache.isEmpty()) {
            return;
        } else {
            final List<Pair<String, String>> localCache = cache.entrySet().stream()
                    .map(entry -> Pair.with(SuffixHelper.shortestSuffix(cache.keySet(),
                            entry.getKey().toAbsolutePath()), entry.getValue())).toList();
            final List<Fork> endpoints = localCache.stream()
                    .map(pair -> new FkRegex("/" + pair.getValue0(),
                            new RsHtml(pair.getValue1()))).map(Fork.class::cast).toList();
            final Fork rootHandler;
            if (localCache.size() == 1) {
                rootHandler = new FkRegex("/",
                        new TkRedirect("/" + localCache.get(0).getValue0()));
            } else {
                final StringBuilder builder = new StringBuilder();
                builder.append("<!DOCTYPE html>").append(System.lineSeparator())
                        .append("<html>").append(System.lineSeparator())
                        .append("\t<head>").append(System.lineSeparator())
                        .append("\t\t<title>Strategic Primer Reports</title>")
                        .append(System.lineSeparator())
                        .append("\t</head>").append(System.lineSeparator())
                        .append("\t<body>").append(System.lineSeparator())
                        .append("\t\t<h1>Strategic Primer Reports</h1>")
                        .append(System.lineSeparator())
                        .append("\t\t<ul>").append(System.lineSeparator());
                for (final Pair<String, String> pair : localCache) {
                    final String file = pair.getValue0();
                    builder.append("\t\t\t<li><a href=\"").append(file).append("\">")
                            .append(file).append("</a></li>")
                            .append(System.lineSeparator());
                }
                builder.append("\t\t</ul>").append(System.lineSeparator())
                        .append("\t</body>").append(System.lineSeparator())
                        .append("</html>");
                rootHandler = new FkRegex("/", new RsHtml(builder.toString()));
            }
            LovelaceLogger.info("About to start serving on port %d", port);
            try {
                new FtBasic(new TkFork(Stream.concat(Stream.of(rootHandler), endpoints.stream()).toArray(Fork[]::new)),
                        port).start(Exit.NEVER);
            } catch (final IOException except) {
                throw new DriverFailedException(except, "I/O error while serving report");
            }
        }
    }

    @Override
    public void startDriver() throws DriverFailedException {
        final String portArgument = options.getArgument("--serve");
        int port;
        try {
            port = Integer.parseInt(portArgument);
        } catch (final NumberFormatException except) {
            if (!"true".equals(portArgument)) {
                LovelaceLogger.warning("Port must be a number");
                LovelaceLogger.trace(except, "Stack trace of port parse failure");
            }
            port = 8080;
        }
        Player player;
        try {
            player = model.getMap().getPlayers().getPlayer(
                    Integer.parseInt(options.getArgument("--player")));
        } catch (final NumberFormatException except) {
            player = null;
        }
        serveReports(port, player);
    }
}

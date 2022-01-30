package drivers;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

import org.javatuples.Pair;

import java.util.stream.Collectors;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.io.BufferedWriter;

import java.io.IOException;
import java.io.IOError;

import drivers.common.IMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.DriverFailedException;
import drivers.common.GUIDriver;
import drivers.common.ReadOnlyDriver;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverFactory;
import drivers.common.ModelDriver;
import drivers.common.SimpleMultiMapModel;
import drivers.common.GUIDriverFactory;
import drivers.common.SimpleDriverModel;
import drivers.common.MapChangeListener;

import drivers.common.cli.ICLIHelper;

import common.map.Player;
import common.map.IMapNG;
import common.map.IMutableMapNG;

import report.ReportGenerator;
import report.TabularReportGenerator;

import javax.swing.JTabbedPane;

import java.awt.Dimension;
import java.awt.Component;

import drivers.gui.common.SPFrame;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.SPFileChooser;
import drivers.gui.common.SPMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import lovelace.util.FileChooser;

import org.takes.facets.fork.Fork;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;

import org.takes.rs.RsHtml;
import org.takes.rs.RsWithType;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsText;

import org.takes.tk.TkRedirect;

import org.takes.http.FtBasic;
import org.takes.http.Exit;


/**
 * A driver to "serve" a report on the contents of a map on an embedded HTTP server.
 */
/* package */ class ReportServingCLI implements ReadOnlyDriver {
	private static final Logger LOGGER = Logger.getLogger(ReportServingCLI.class.getName());
	public ReportServingCLI(SPOptions options, IDriverModel model, ICLIHelper cli) {
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

	private void serveReports(int port, @Nullable Player currentPlayer) throws DriverFailedException {
		Map<Path, String> cache = new HashMap<Path, String>();
		if (model instanceof IMultiMapModel) {
			for (IMapNG map : ((IMultiMapModel) model).getAllMaps()) {
				Path file = map.getFilename();
				// TODO: skip if no filename
				if (!cache.containsKey(file.toString())) {
					try {
						cache.put(file, ReportGenerator.createReport(map, cli,
							Optional.of(currentPlayer).orElse(map.getCurrentPlayer())));
					} catch (IOException except) {
						throw new DriverFailedException(except, "I/O error while creating report(s)");
					}
				}
			}
		} else if (model.getMap().getFilename() != null) {
			try {
				cache.put(model.getMap().getFilename(), ReportGenerator.createReport(model.getMap(), cli,
					Optional.of(currentPlayer).orElse(model.getMap().getCurrentPlayer())));
			} catch (IOException except) {
				throw new DriverFailedException(except, "I/O error while creating report(s)");
			}
		}
		if (cache.isEmpty()) {
			return;
		} else {
			List<Pair<String, String>> localCache = cache.entrySet().stream()
				.map(entry -> Pair.with(SuffixHelper.shortestSuffix(cache.keySet(),
					entry.getKey().toAbsolutePath()), entry.getValue()))
				.collect(Collectors.toList());
			List<Fork> endpoints = localCache.stream()
				.map(pair -> new FkRegex("/" + pair.getValue0(),
					new RsHtml(pair.getValue1())))
				.collect(Collectors.toList());
			Fork rootHandler;
			if (localCache.size() == 1) {
				rootHandler = new FkRegex("/",
					new TkRedirect("/" + localCache.get(0).getValue0()));
			} else {
				StringBuilder builder = new StringBuilder();
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
				for (Pair<String, String> pair : localCache) {
					String file = pair.getValue0();
					builder.append("\t\t\t<li><a href=\"").append(file).append("\">")
						.append(file).append("</a></li>")
						.append(System.lineSeparator());
				}
				builder.append("\t\t</ul>").append(System.lineSeparator())
					.append("\t</body>").append(System.lineSeparator())
					.append("</html>");
				rootHandler = new FkRegex("/", new RsHtml(builder.toString()));
			}
			LOGGER.info("About to start serving on port " + port);
			try {
				new FtBasic(new TkFork(Stream.concat(Stream.of(rootHandler), endpoints.stream()).toArray(Fork[]::new)),
					port).start(Exit.NEVER);
			} catch (IOException except) {
				throw new DriverFailedException(except, "I/O error while serving report");
			}
		}
	}

	@Override
	public void startDriver() throws DriverFailedException {
		String portArgument = options.getArgument("--serve");
		int port;
		try {
			port = Integer.parseInt(portArgument);
		} catch (NumberFormatException except) {
			if (!"true".equals(portArgument)) {
				LOGGER.warning("Port must be a number");
				LOGGER.log(Level.FINER, "Stack trace of port parse failure", except);
			}
			port = 8080;
		}
		Player player;
		try {
			player = model.getMap().getPlayers().getPlayer(
				Integer.parseInt(options.getArgument("--player")));
		} catch (NumberFormatException except) {
			player = null;
		}
		serveReports(port, player);
	}
}

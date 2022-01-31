package drivers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lovelace.util.ThrowingBiConsumer;
import lovelace.util.ThrowingConsumer;
import lovelace.util.ThrowingFunction;
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

/* package */ class TabularReportServingCLI implements ReadOnlyDriver {
	private static final Logger LOGGER = Logger.getLogger(TabularReportServingCLI.class.getName());
	public TabularReportServingCLI(ICLIHelper cli, SPOptions options, IDriverModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
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

	private void serveReports(int port) throws DriverFailedException {
		Map<Path, IMapNG> mapping;
		if (model instanceof IMultiMapModel) {
			mapping = StreamSupport.stream(
					((IMultiMapModel) model).getAllMaps().spliterator(), false)
				.collect(Collectors.toMap(
					map -> Optional.ofNullable(map.getFilename()).orElseGet(
						() -> Paths.get("unknown.xml")),
					map -> map));
		} else if (model.getMap().getFilename() != null) {
			mapping = Collections.singletonMap(model.getMap().getFilename(), model.getMap());
		} else {
			mapping = Collections.singletonMap(Paths.get("unknown.xml"), model.getMap());
		}

		Map<Pair<String, String>, StringBuilder> builders = new HashMap<>();

		ThrowingFunction<Path, ThrowingFunction<String, ThrowingConsumer<String, IOException>, IOException>, IOException> filenameFunction = base -> {
			String baseName = SuffixHelper.shortestSuffix(mapping.keySet(), base);
			return tableName -> {
				Pair<String, String> key = Pair.with(baseName, tableName);
				if (builders.containsKey(key)) {
					return builders.get(key)::append;
				} else {
					StringBuilder writer = new StringBuilder();
					builders.put(key, writer);
					return writer::append;
				}
			};
		};

		ThrowingBiConsumer<IMapNG, @Nullable Path, DriverFailedException> createReports =
			(map, mapFile) -> {
				if (mapFile != null) { // TODO: invert
					try {
						TabularReportGenerator.createTabularReports(map,
							filenameFunction.apply(mapFile), cli);
					} catch (IOException|IOError except) {
						throw new DriverFailedException(except);
					}
				} else {
					LOGGER.severe("Asked to create reports from map with no filename");
					// TODO: Pass in "unknown.xml", surely?
				}
			};

		if (model instanceof IMultiMapModel) {
			for (IMapNG map : ((IMultiMapModel) model).getAllMaps()) {
				createReports.accept(map, Optional.ofNullable(map.getFilename())
					.orElseGet(() -> Paths.get("unknown.xml")));
			}
		} else {
			createReports.accept(model.getMap(), Optional.ofNullable(model.getMap().getFilename())
				.orElseGet(() -> Paths.get("unknown.xml")));
		}

		// [file, table]->builder
		List<Fork> endpoints = builders.entrySet().stream()
			.map(entry -> new FkRegex(String.format("/%s.%s.csv", entry.getKey().getValue0(),
					entry.getKey().getValue1()),
				new RsWithType(new RsWithHeader(new RsText(entry.getValue().toString()),
					"Content-Disposition", String.format(
						"attachment; filename=\"%s.csv\"", entry.getValue())),
					"text/csv"))).collect(Collectors.toList()); // TODO: Keep as Stream instead of collecting yet?

		Function<String, String> tocHtml = path -> {
			StringBuilder builder = new StringBuilder();
			builder.append("<!DOCTYPE html>").append(System.lineSeparator())
				.append("<html>").append(System.lineSeparator())
				.append("\t<head>").append(System.lineSeparator())
				.append("\t\t<title>Tabular reports for ").append(path).append("</title>")
					.append(System.lineSeparator())
				.append("\t</head>").append(System.lineSeparator())
				.append("\t<body>").append(System.lineSeparator())
				.append("\t\t<h1>Tabular reports for ").append(path).append("</h1>")
					.append(System.lineSeparator())
				.append("\t\t<ul>").append(System.lineSeparator());
			for (Pair<String, String> pair : builders.keySet()) {
				if (path.equals(pair.getValue0())) {
					builder.append("\t\t\t<li><a href=\"/").append(pair.getValue0())
						.append(".").append(pair.getValue1()).append(".csv\">")
						.append(pair.getValue1()).append(".csv</a></li>")
						.append(System.lineSeparator());
				}
			}
			builder.append("\t\t</ul>").append(System.lineSeparator())
				.append("\t</body>").append(System.lineSeparator())
				.append("</html>").append(System.lineSeparator());
			return builder.toString();
		};

		List<Fork> tocs = mapping.keySet().stream()
			.map(key -> SuffixHelper.shortestSuffix(mapping.keySet(), key))
			.flatMap(path -> Stream.of(new FkRegex("/" + path, new RsHtml(tocHtml.apply(path))),
				new FkRegex(String.format("/%s/", path), new RsHtml(tocHtml.apply(path)))))
			.collect(Collectors.toList());

		StringBuilder rootDocument = new StringBuilder();
		rootDocument.append("<!DOCTYPE html>").append(System.lineSeparator())
			.append("<html>").append(System.lineSeparator())
			.append("\t<head>").append(System.lineSeparator())
			.append("\t\t<title>Strategic Primer Tabular Reports</title>")
				.append(System.lineSeparator())
			.append("\t</head>").append(System.lineSeparator())
			.append("\t<body>").append(System.lineSeparator())
			.append("\t\t<h1>Strategic Primer Tabular Reports</h1>")
				.append(System.lineSeparator())
			.append("\t\t<ul>").append(System.lineSeparator());
		mapping.keySet().stream().map(key -> SuffixHelper.shortestSuffix(mapping.keySet(), key))
			.forEach(file ->
				rootDocument.append("\t\t\t<li><a href=\"/").append(file).append("\">")
					.append(file).append("</a></li>").append(System.lineSeparator()));
		rootDocument.append("\t\t</ul>").append(System.lineSeparator());
		rootDocument.append("\t</body>").append(System.lineSeparator());
		rootDocument.append("</html>").append(System.lineSeparator());

		LOGGER.info("About to start serving on port ``port``");
		try {
			new FtBasic(
				new TkFork(Stream.<Fork>concat(Stream.<Fork>of(new FkRegex("/", new RsHtml(rootDocument.toString())),
					new FkRegex("/index.html", new RsHtml(rootDocument.toString()))),
					Stream.concat(tocs.stream(), endpoints.stream())).toArray(Fork[]::new)), port)
				.start(Exit.NEVER);
		} catch (IOException except) {
			throw new DriverFailedException(except, "I/O error while serving files");
		}
	}

	@Override
	public void startDriver() throws DriverFailedException {
		String portArgument = options.getArgument("--serve");
		int port;
		try {
			port = Integer.parseInt(portArgument);
		} catch (NumberFormatException except) {
			if (!portArgument.equals("true")) {
				LOGGER.warning("Port must be a number");
				LOGGER.log(Level.FINER, "Stack trace of port parse failure", except);
			}
			port = 8080;
		}
		serveReports(port);
	}
}
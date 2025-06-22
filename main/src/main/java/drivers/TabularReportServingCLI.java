package drivers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import java.util.stream.Stream;

import lovelace.util.LovelaceLogger;
import lovelace.util.ThrowingBiConsumer;
import lovelace.util.ThrowingConsumer;
import lovelace.util.ThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.javatuples.Pair;

import java.util.stream.Collectors;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.IOException;
import java.io.IOError;

import drivers.common.IMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.SPOptions;
import drivers.common.DriverFailedException;
import drivers.common.ReadOnlyDriver;

import drivers.common.cli.ICLIHelper;

import legacy.map.ILegacyMap;

import report.TabularReportGenerator;

import java.util.HashMap;
import java.util.Map;

import org.takes.facets.fork.Fork;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;

import org.takes.rs.RsHtml;
import org.takes.rs.RsWithType;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsText;

import org.takes.http.FtBasic;
import org.takes.http.Exit;

/* package */ final class TabularReportServingCLI implements ReadOnlyDriver {
	private static final int PORT = 8080;

	public TabularReportServingCLI(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
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

	@FunctionalInterface
	private interface IOThrowingFunction<I, O> extends ThrowingFunction<I, O, IOException> {}

	@FunctionalInterface
	private interface IOThrowingConsumer<T> extends ThrowingConsumer<T, IOException> {}

	private void serveReports(final int port) throws DriverFailedException {
		final Map<Path, ILegacyMap> mapping;
		if (model instanceof final IMultiMapModel mmm) {
			mapping = mmm.streamAllMaps()
					.collect(Collectors.toMap(
							map -> Optional.ofNullable(map.getFilename()).orElseGet(
									() -> Paths.get("unknown.xml")),
							Function.identity()));
		} else if (Objects.nonNull(model.getMap().getFilename())) {
			mapping = Collections.singletonMap(model.getMap().getFilename(), model.getMap());
		} else {
			mapping = Collections.singletonMap(Paths.get("unknown.xml"), model.getMap());
		}

		final Map<Pair<String, String>, StringBuilder> builders = new HashMap<>();

		final ThrowingBiConsumer<ILegacyMap, @Nullable Path, DriverFailedException> createReports =
				getReportCreator(mapping, builders);

		if (model instanceof final IMultiMapModel mmm) {
			for (final ILegacyMap map : mmm.getAllMaps()) {
				createReports.accept(map, Optional.ofNullable(map.getFilename())
						.orElseGet(() -> Paths.get("unknown.xml")));
			}
		} else {
			createReports.accept(model.getMap(), Optional.ofNullable(model.getMap().getFilename())
					.orElseGet(() -> Paths.get("unknown.xml")));
		}

		// [file, table]->builder
		final Stream<Fork> endpoints = builders.entrySet().stream()
				.map(entry -> new FkRegex("/%s.%s.csv".formatted(entry.getKey().getValue0(),
						entry.getKey().getValue1()),
						new RsWithType(new RsWithHeader(new RsText(entry.getValue().toString()),
								"Content-Disposition", "attachment; filename=\"%s.csv\"".formatted(entry.getValue())),
								"text/csv")));

		final Function<String, String> tocHtml = path -> {
			final StringBuilder builder = new StringBuilder();
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
			for (final Pair<String, String> pair : builders.keySet()) {
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

		final List<Fork> tocs = mapping.keySet().stream()
				.map(key -> SuffixHelper.shortestSuffix(mapping.keySet(), key))
				.flatMap(path -> Stream.of(new FkRegex("/" + path, new RsHtml(tocHtml.apply(path))),
						new FkRegex("/%s/".formatted(path), new RsHtml(tocHtml.apply(path)))))
				.map(Fork.class::cast).toList();

		final StringBuilder rootDocument = new StringBuilder();
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

		LovelaceLogger.info("About to start serving on port %d", port);
		try {
			new FtBasic(
					new TkFork(Stream.concat(Stream.<Fork>of(new FkRegex("/", new RsHtml(rootDocument.toString())),
									new FkRegex("/index.html", new RsHtml(rootDocument.toString()))),
							Stream.concat(tocs.stream(), endpoints)).toArray(Fork[]::new)), port)
					.start(Exit.NEVER);
		} catch (final IOException except) {
			throw new DriverFailedException(except, "I/O error while serving files");
		}
	}

	private @NotNull ThrowingBiConsumer<ILegacyMap, @Nullable Path, DriverFailedException> getReportCreator(
			final Map<Path, ILegacyMap> mapping, final Map<Pair<String, String>, StringBuilder> builders) {
		final IOThrowingFunction<Path, IOThrowingFunction<String, IOThrowingConsumer<String>>> filenameFunction =
				base -> {
					final String baseName = SuffixHelper.shortestSuffix(mapping.keySet(), base);
					return tableName -> builders.computeIfAbsent(Pair.with(baseName, tableName),
							k -> new StringBuilder())::append;
				};

		return (map, mapFile) -> {
					try {
						if (Objects.isNull(mapFile)) {
							LovelaceLogger.error("Asked to create reports from map with no filename");
							TabularReportGenerator.createTabularReports(map,
									filenameFunction.apply(Paths.get("unknown.xml")), cli);
						} else {
							TabularReportGenerator.createTabularReports(map,
									filenameFunction.apply(mapFile), cli);
						}
					} catch (final IOException | IOError except) {
						throw new DriverFailedException(except);
					}
				};
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
			port = PORT;
		}
		serveReports(port);
	}
}

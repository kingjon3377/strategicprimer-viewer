package drivers;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.logging.Level;

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

import report.TabularReportGenerator;
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
 * A driver to produce tabular (CSV) reports of the contents of a player's map.
 */
public class TabularReportCLI implements ReadOnlyDriver {
	private static final Logger LOGGER = Logger.getLogger(TabularReportCLI.class.getName());
	public TabularReportCLI(ICLIHelper cli, SPOptions options, IDriverModel model) {
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

	private final Map<String, BufferedWriter> writers = new HashMap<>();

	private ThrowingFunction<String, ThrowingConsumer<String, IOException>, IOException> filenameFunction(Path base) {
		String baseName = base.getFileName().toString();
		return tableName -> {
			String key = String.format("%s.%s.csv", baseName, tableName);
			if (writers.containsKey(key)) {
				return writers.get(key)::write;
			} else {
				// FIXME: Looks like this leaks the writer
				BufferedWriter writer = Files.newBufferedWriter(base.resolveSibling(key));
				writers.put(key, writer);
				return writer::write;
			}
		};
	}

	private void createReports(IMapNG map, @Nullable Path mapFile) throws DriverFailedException {
		if (mapFile != null) { // TODO: invert
			try {
				TabularReportGenerator.createTabularReports(map,
					filenameFunction(mapFile), cli);
			} catch (IOException|IOError except) {
				throw new DriverFailedException(except);
			}
		} else {
			LOGGER.severe("Asked to create reports from map with no filename");
			// TODO: substitute "unknown.xml", surely?
		}
	}

	@Override
	public void startDriver() throws DriverFailedException {
		try {
			if (model instanceof IMultiMapModel) {
				for (IMapNG map : ((IMultiMapModel) model).getAllMaps()) {
					createReports(map, map.getFilename());
				}
			} else {
				createReports(model.getMap(), model.getMap().getFilename());
			}
		} finally {
			try {
				for (BufferedWriter writer : writers.values()) {
					writer.close();
				}
			} catch (IOException except) {
				LOGGER.log(Level.SEVERE, "I/O error closing writer(s)", except);
			}
		}
	}
}

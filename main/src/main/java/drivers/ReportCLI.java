package drivers;

import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;
import java.util.logging.Level;

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
 * A driver to produce a report of the contents of a map.
 */
public class ReportCLI implements ReadOnlyDriver {
	private static final Logger LOGGER = Logger.getLogger(ReportCLI.class.getName());
	public ReportCLI(SPOptions options, IDriverModel model, ICLIHelper cli) {
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

	private void writeReport(@Nullable Path filename, IMapNG map) throws IOException {
		if (filename != null) { // TODO: invert
			Player player;
			if (options.hasOption("--player")) {
				try {
					player = map.getPlayers().getPlayer(Integer.parseInt(
						options.getArgument("--player")));
				} catch (NumberFormatException except) {
					LOGGER.log(Level.WARNING, "Non-numeric player", except);
					player = map.getCurrentPlayer();
				}
			} else {
				player = map.getCurrentPlayer();
			}
			String outString;
			Path outPath;
			if (options.hasOption("--out")) {
				outString = options.getArgument("--out");
				outPath = Paths.get(outString);
			} else {
				outString = filename.getFileName().toString() + ".report.html";
				outPath = filename.resolveSibling(outString);
			}
			try (BufferedWriter writer = Files.newBufferedWriter(outPath,
					StandardCharsets.UTF_8, StandardOpenOption.WRITE,
					StandardOpenOption.CREATE_NEW)) {
				writer.write(ReportGenerator.createReport(map, cli, player));
			}
		} else {
			LOGGER.severe("Asked to make report from map with no filename");
		}
	}

	@Override
	public void startDriver() throws DriverFailedException {
		try {
			if (model instanceof IMultiMapModel) {
				for (IMapNG map : ((IMultiMapModel) model).getAllMaps()) {
					writeReport(map.getFilename(), map);
				}
			} else {
				writeReport(model.getMap().getFilename(), model.getMap());
			}
		} catch (IOException except) {
			throw new DriverFailedException(except, "I/O error while writing report(s)");
		}
	}
}


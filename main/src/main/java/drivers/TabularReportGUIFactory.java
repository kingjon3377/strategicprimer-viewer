package drivers;

import java.util.Collections;
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

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to show tabular reports of the contents of a player's map in a GUI.
 */
@AutoService(DriverFactory.class)
public class TabularReportGUIFactory implements GUIDriverFactory {
	private static final Logger LOGGER = Logger.getLogger(TabularReportGUIFactory.class.getName());
	private static final IDriverUsage USAGE = new DriverUsage(true, "tabular-report",
		ParamCount.One, "Tabular Report Viewer", "Show the contents of a map in tabular form",
		false, true);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}


	/**
	 * Ask the user to choose a file.
	 */
	@Override // TODO: Make interface allow throwing DriverFailedException
	public Iterable<Path> askUserForFiles() {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (FileChooser.ChoiceInterruptedException except) {
			LOGGER.log(Level.WARNING, "Choice interrupted or user didn't choose", except);
			return Collections.emptyList();
		}
	}

	@Override
	public GUIDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		return new TabularReportGUI(cli, options, model);
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new SimpleDriverModel(map);
	}
}


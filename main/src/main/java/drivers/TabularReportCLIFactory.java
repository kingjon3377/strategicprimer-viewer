package drivers;

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
 * A factory for a driver to produce tabular (CSV) reports of the contents of a player's map.
 */
@AutoService(DriverFactory.class)
public class TabularReportCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "tabular-report",
		ParamCount.AtLeastOne, "Tabular Report Generator",
		"Produce CSV reports of the contents of a map.", true, false, "--serve[=8080]");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (options.hasOption("--serve")) {
			return new TabularReportServingCLI(cli, options, model);
		} else {
			return new TabularReportCLI(cli, options, model);
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new SimpleMultiMapModel(map);
	}
}

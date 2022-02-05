package drivers;

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
 * A factory for a driver to produce a report of the contents of a map.
 */
@AutoService(DriverFactory.class)
public class ReportCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "create-report",
		ParamCount.AtLeastOne, "Report Generator", "Produce HTML report of the contents of a map",
		true, false,
		('\\' == File.separatorChar) ?  "--out=C:\\path\\to\\output.html"
			: "--out=/path/to/output.html",
		"--player=NN", "--current-turn=NN", "--serve[=8080]");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (options.hasOption("--serve")) {
			return new ReportServingCLI(options, model, cli);
		} else {
			return new ReportCLI(options, model, cli);
		}
	}

	@Override
	public IDriverModel createModel(final IMutableMapNG map) {
		return new SimpleMultiMapModel(map);
	}
}

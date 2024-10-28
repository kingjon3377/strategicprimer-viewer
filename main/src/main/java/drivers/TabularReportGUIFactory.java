package drivers;

import drivers.common.DriverFailedException;

import java.util.EnumSet;
import java.util.List;

import java.nio.file.Path;

import drivers.common.IDriverModel;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.GUIDriver;
import drivers.common.DriverFactory;
import drivers.common.GUIDriverFactory;

import drivers.common.cli.ICLIHelper;

import drivers.gui.common.SPFileChooser;

import lovelace.util.FileChooser;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to show tabular reports of the contents of a player's map in a GUI.
 */
@AutoService(DriverFactory.class)
public final class TabularReportGUIFactory implements GUIDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.Graphical, "tabular-report",
			ParamCount.One, "Tabular Report Viewer", "Show the contents of a map in tabular form",
			EnumSet.of(IDriverUsage.DriverMode.Graphical), "--hq-row=NN --hq-col=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}


	/**
	 * Ask the user to choose a file.
	 */
	@Override
	public List<Path> askUserForFiles() throws DriverFailedException {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			throw new DriverFailedException(except, "Choice interrupted or user didn't choose");
		}
	}

	@Override
	public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		return new TabularReportGUI(cli, options, model);
	}
}


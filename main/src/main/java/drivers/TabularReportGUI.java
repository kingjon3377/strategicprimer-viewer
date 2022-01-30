package drivers;

import drivers.map_viewer.ViewerGUIFactory;
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


/**
 * A driver to show tabular reports of the contents of a player's map in a GUI.
 */
public class TabularReportGUI implements GUIDriver {
	private static final Logger LOGGER = Logger.getLogger(TabularReportGUI.class.getName());
	public TabularReportGUI(ICLIHelper cli, SPOptions options, IDriverModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	private final ICLIHelper cli;
	private final IDriverModel model;
	private final SPOptions options;

	@Override
	public IDriverModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public void startDriver() {
		SPFrame window = new SPFrame("Tabular Report", this, new Dimension(640, 480));
		JTabbedPane frame = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		MapChangeListener listener = new MapChangeListener() {
			@Override
			public void mapChanged() {
				frame.removeAll();
				try {
					TabularReportGenerator.createGUITabularReports(frame::addTab, model.getMap());
				} catch (IOException except) {
					// FIXME: Show error dialog
					LOGGER.log(Level.SEVERE, "I/O error while generating tabular reports", except);
				}
			}

			@Override
			public void mapMetadataChanged() {}
		};
		listener.mapChanged();
		model.addMapChangeListener(listener);
		window.add(frame);
		window.setJMenuBar(SPMenu.forWindowContaining(frame,
			SPMenu.createFileMenu(
				new IOHandler(this, new ViewerGUIFactory()::createDriver, cli)::actionPerformed,
				this),
			SPMenu.disabledMenu(SPMenu.createMapMenu(x -> {}, this)),
			SPMenu.disabledMenu(SPMenu.createViewMenu(x -> {}, this))));
			window.addWindowListener(new WindowCloseListener(ignored -> window.dispose()));
		window.showWindow();
	}

	/**
	 * Ask the user to choose a file.
	 */
	@Override
	public Iterable<Path> askUserForFiles() {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (FileChooser.ChoiceInterruptedException except) {
			LOGGER.log(Level.WARNING, "Choice interrupted or user didn't choose", except);
			return Collections.emptyList();
		}
	}

	@Override
	public void open(IMutableMapNG map) {
		model.setMap(map);
	}
}

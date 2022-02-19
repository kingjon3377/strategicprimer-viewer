package drivers;

import drivers.common.DriverFailedException;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.nio.file.Path;

import java.io.IOException;

import drivers.common.IDriverModel;
import drivers.common.SPOptions;
import drivers.common.GUIDriver;
import drivers.common.MapChangeListener;

import drivers.common.cli.ICLIHelper;

import common.map.IMutableMapNG;

import javax.swing.SwingConstants;
import lovelace.util.ShowErrorDialog;
import report.TabularReportGenerator;

import javax.swing.JTabbedPane;

import java.awt.Dimension;

import drivers.gui.common.SPFrame;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.SPFileChooser;
import drivers.gui.common.SPMenu;

import lovelace.util.FileChooser;


/**
 * A driver to show tabular reports of the contents of a player's map in a GUI.
 */
public class TabularReportGUI implements GUIDriver {
	private static final Logger LOGGER = Logger.getLogger(TabularReportGUI.class.getName());
	public TabularReportGUI(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
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
		final SPFrame window = new SPFrame("Tabular Report", this, new Dimension(640, 480));
		final JTabbedPane frame = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		final MapChangeListener listener = new MapChangeListener() {
			@Override
			public void mapChanged() {
				frame.removeAll();
				try {
					TabularReportGenerator.createGUITabularReports(frame::addTab, model.getMap());
				} catch (final IOException except) {
					ShowErrorDialog.showErrorDialog(window, "Strategic Primer Tabular Reports",
							String.format("I/O error while generating reports:%n%s", except.getLocalizedMessage()));
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
			SPMenu.createFileMenu(new IOHandler(this, cli), this),
			SPMenu.disabledMenu(SPMenu.createMapMenu(x -> {}, this)),
			SPMenu.disabledMenu(SPMenu.createViewMenu(x -> {}, this))));
			window.addWindowListener(new WindowCloseListener(ignored -> window.dispose()));
		window.showWindow();
	}

	/**
	 * Ask the user to choose a file.
	 */
	@Override
	public Iterable<Path> askUserForFiles() throws DriverFailedException {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			throw new DriverFailedException(except, "Choice interrupted or user didn't choose");
		}
	}

	@Override
	public void open(final IMutableMapNG map) {
		model.setMap(map);
	}
}

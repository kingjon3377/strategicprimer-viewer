package drivers.advancement;

import drivers.map_viewer.ViewerGUIFactory;
import java.io.IOException;
import java.util.Collections;
import javax.swing.SwingUtilities;

import drivers.common.SPOptions;
import drivers.common.PlayerChangeListener;
import drivers.common.MultiMapGUIDriver;
import drivers.common.IWorkerModel;
import drivers.common.WorkerGUI;

import drivers.common.cli.ICLIHelper;

import worker.common.WorkerModel;

import drivers.gui.common.about.AboutDialog;

import drivers.PlayerChangeMenuListener;
import drivers.IOHandler;

import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.MenuBroker;
import drivers.gui.common.SPFileChooser;

import java.awt.event.ActionEvent;

import common.map.IMutableMapNG;

import lovelace.util.FileChooser;

import java.nio.file.Path;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.stream.StreamSupport;

/**
 * The worker-advancement GUI driver.
 *
 * TODO: Does this really need to be public?
 */
public class AdvancementGUI implements MultiMapGUIDriver, WorkerGUI {
	private static final Logger LOGGER = Logger.getLogger(AdvancementGUI.class.getName());
	public AdvancementGUI(final ICLIHelper cli, final SPOptions options, final IWorkerModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	private final ICLIHelper cli;

	private final SPOptions options;
	private final IWorkerModel model;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public IWorkerModel getModel() {
		return model;
	}

	private void reload(final PlayerChangeListener frame) {
		frame.playerChanged(model.getCurrentPlayer(), model.getCurrentPlayer());
	}

	private void createWindow(final MenuBroker menuHandler, final PlayerChangeMenuListener pcml) {
		AdvancementFrame frame = new AdvancementFrame(model, menuHandler, this);
		frame.addWindowListener(new WindowCloseListener(menuHandler::actionPerformed));
		pcml.addPlayerChangeListener(frame);
		menuHandler.register(ignored -> reload(frame), "reload tree");
		try {
			menuHandler.registerWindowShower(new AboutDialog(frame, frame.getWindowName()), "about");
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error loading About dialog text", except);
			// But this isn't a blocker to the driver as a whole, so keep going.
		}
		if (StreamSupport.stream(model.getAllMaps().spliterator(), true)
				.noneMatch(m ->
					model.getUnits(m.getCurrentPlayer()).iterator().hasNext())) {
			pcml.actionPerformed(new ActionEvent(frame, ActionEvent.ACTION_FIRST,
				"change current player"));
		}
		frame.showWindow();
	}

	@Override
	public void startDriver() {
		MenuBroker menuHandler = new MenuBroker();
		menuHandler.register(new IOHandler(this, new ViewerGUIFactory()::createDriver, cli), "load", "save", "save as", "new",
			"load secondary", "save all", "open in map viewer",
			"open secondary map in map viewer", "close", "quit");
		PlayerChangeMenuListener pcml = new PlayerChangeMenuListener(model);
		menuHandler.register(pcml, "change current player");
		SwingUtilities.invokeLater(() -> createWindow(menuHandler, pcml));
	}

	/**
	 * Ask the user to choose a file or files.
	 */
	@Override
	public Iterable<Path> askUserForFiles() {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			// TODO: throw as DriverFailedException once interface permits
			LOGGER.log(Level.WARNING, "Choice interrupted or user didn't choose", except);
			return Collections.emptyList();
		}
	}

	@Override
	public void open(final IMutableMapNG map) {
		if (model.isMapModified()) {
			SwingUtilities.invokeLater(() -> new AdvancementGUI(cli, options,
				new WorkerModel(map)).startDriver());
		} else {
			model.setMap(map);
		}
	}
}

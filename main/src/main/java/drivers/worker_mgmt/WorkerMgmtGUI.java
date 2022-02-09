package drivers.worker_mgmt;

import drivers.common.DriverFailedException;
import java.io.IOException;
import java.util.logging.Level;
import java.nio.file.Path;
import javax.swing.SwingUtilities;
import drivers.gui.common.about.AboutDialog;
import drivers.PlayerChangeMenuListener;
import drivers.IOHandler;
import java.util.logging.Logger;
import drivers.common.cli.ICLIHelper;
import drivers.common.SPOptions;
import drivers.common.MultiMapGUIDriver;
import drivers.common.IWorkerModel;
import drivers.common.WorkerGUI;
import worker.common.WorkerModel;
import java.awt.event.ActionEvent;
import common.map.IMutableMapNG;
import lovelace.util.FileChooser;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.MenuBroker;
import drivers.gui.common.SPFileChooser;

/**
 * A driver to start the worker management GUI.
 */
public class WorkerMgmtGUI implements MultiMapGUIDriver, WorkerGUI {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(WorkerMgmtGUI.class.getName());

	public WorkerMgmtGUI(final ICLIHelper cli, final SPOptions options, final IWorkerModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	private final ICLIHelper cli;

	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	private final IWorkerModel model;

	@Override
	public IWorkerModel getModel() {
		return model;
	}

	private void createWindow(final MenuBroker menuHandler, final PlayerChangeMenuListener pcml)
			throws DriverFailedException {
		LOGGER.finer("Inside GUI creation lambda");
		final WorkerMgmtFrame frame = new WorkerMgmtFrame(options, model, menuHandler, this);
		LOGGER.finer("Created worker mgmt frame");
		pcml.addPlayerChangeListener(frame);
		LOGGER.finer("Added it as a listener on the PCML");
		frame.addWindowListener(new WindowCloseListener(menuHandler::actionPerformed));
		menuHandler.register(ignored -> frame.playerChanged(model.getCurrentPlayer(), model.getCurrentPlayer()),
				"reload tree");
		try {
			menuHandler.registerWindowShower(new AboutDialog(frame,
				frame.getWindowName()), "about");
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error setting up About dialog", except);
		}
		LOGGER.finer("Registered menu handlers");
		if (model.streamAllMaps().allMatch(m -> model.getUnits(m.getCurrentPlayer()).isEmpty())) {
			pcml.actionPerformed(new ActionEvent(frame, ActionEvent.ACTION_FIRST,
				"change current player"));
		}

		LOGGER.finer("About to show window");
		frame.showWindow();
		LOGGER.finer("Window should now be visible");
	}

	@Override
	public void startDriver() throws DriverFailedException {
		final MenuBroker menuHandler = new MenuBroker();
		menuHandler.register(new IOHandler(this, cli), "load",
			"save", "save as", "new", "load secondary", "save all", "open in map viewer",
			"open secondary map in map viewer", "close", "quit");
		final PlayerChangeMenuListener pcml = new PlayerChangeMenuListener(model);
		menuHandler.register(pcml, "change current player");
		try {
			SwingUtilities.invokeLater(() -> {
					try {
						createWindow(menuHandler, pcml);
					} catch (final DriverFailedException except) {
						throw new RuntimeException(except);
					}
				});
		} catch (final RuntimeException except) {
			if (except.getCause() instanceof DriverFailedException) {
				throw (DriverFailedException) except.getCause();
			} else {
				throw except;
			}
		}
		LOGGER.finer("Worker GUI window should appear any time now");
	}

	/**
	 * Ask the user to choose a file or files.
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
		if (model.isMapModified()) {
			SwingUtilities.invokeLater(() -> {
					try {
						new WorkerMgmtGUI(cli, options, new WorkerModel(map))
							.startDriver();
					} catch (final DriverFailedException except) {
						// FIXME: Show error dialog
						LOGGER.log(Level.SEVERE, "Failed to open new window",
							except);
					}
				});
		} else {
			model.setMap(map);
		}
	}
}

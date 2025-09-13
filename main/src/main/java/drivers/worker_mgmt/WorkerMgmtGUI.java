package drivers.worker_mgmt;

import drivers.common.DriverFailedException;

import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import drivers.gui.common.IMenuBroker;
import drivers.gui.common.about.AboutDialog;
import drivers.PlayerChangeMenuListener;
import drivers.IOHandler;
import drivers.common.cli.ICLIHelper;
import drivers.common.SPOptions;
import drivers.common.MultiMapGUIDriver;
import drivers.common.IWorkerModel;
import drivers.common.WorkerGUI;
import lovelace.util.LovelaceLogger;
import worker.common.WorkerModel;

import java.awt.event.ActionEvent;

import legacy.map.IMutableLegacyMap;
import lovelace.util.FileChooser;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.MenuBroker;
import drivers.gui.common.SPFileChooser;

/**
 * A driver to start the worker management GUI.
 */
public final class WorkerMgmtGUI implements MultiMapGUIDriver, WorkerGUI {
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

	private void createWindow(final IMenuBroker menuHandler, final PlayerChangeMenuListener pcml) {
		LovelaceLogger.trace("Inside GUI creation lambda");
		final WorkerMgmtFrame frame = new WorkerMgmtFrame(options, model, menuHandler, WorkerMgmtGUI.class);
		LovelaceLogger.trace("Created worker mgmt frame");
		pcml.addPlayerChangeListener(frame);
		LovelaceLogger.trace("Added it as a listener on the PCML");
		frame.addWindowListener(new WindowCloseListener(menuHandler));
		menuHandler.register(ignored -> frame.playerChanged(model.getCurrentPlayer(), model.getCurrentPlayer()),
				"reload tree");
		try {
			menuHandler.registerWindowShower(new AboutDialog(frame,
					frame.getWindowName()), "about");
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LovelaceLogger.error(except, "I/O error setting up About dialog");
		}
		LovelaceLogger.trace("Registered menu handlers");
		if (model.streamAllMaps().allMatch(m -> model.getUnits(m.getCurrentPlayer()).isEmpty())) {
			pcml.actionPerformed(new ActionEvent(frame, ActionEvent.ACTION_FIRST,
					"change current player"));
		}

		LovelaceLogger.trace("About to show window");
		frame.showWindow();
		LovelaceLogger.trace("Window should now be visible");
	}

	@Override
	public void startDriver() throws DriverFailedException {
		final IMenuBroker menuHandler = new MenuBroker();
		menuHandler.register(new IOHandler(this, cli), "load",
				"save", "save as", "new", "load secondary", "save all", "open in map viewer",
				"open secondary map in map viewer", "close", "quit");
		final PlayerChangeMenuListener pcml = new PlayerChangeMenuListener(model);
		menuHandler.register(pcml, "change current player");
		try {
			SwingUtilities.invokeLater(() -> createWindow(menuHandler, pcml));
		} catch (final RuntimeException except) {
			if (except.getCause() instanceof final DriverFailedException dfe) {
				throw dfe;
			} else {
				throw except;
			}
		}
		LovelaceLogger.trace("Worker GUI window should appear any time now");
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
	public void open(final IMutableLegacyMap map) {
		switch (model.getMapStatus()) {
			case Modified -> SwingUtilities.invokeLater(() -> {
				try {
					new WorkerMgmtGUI(cli, options, new WorkerModel(map))
							.startDriver();
				} catch (final DriverFailedException except) {
					JOptionPane.showMessageDialog(null,
							"Failed to open new window:%n%s".formatted(except.getMessage()),
							"Strategic Primer Worker Management", JOptionPane.ERROR_MESSAGE);
					LovelaceLogger.error(except, "Failed to open new window");
				}
			});
			case Unmodified -> model.setMap(map);
		}
	}
}

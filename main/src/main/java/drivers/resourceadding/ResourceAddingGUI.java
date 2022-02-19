package drivers.resourceadding;

import drivers.common.DriverFailedException;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.NoSuchFileException;

import java.util.function.Consumer;

import impl.xmlio.MapIOHelper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import drivers.common.cli.ICLIHelper;

import java.awt.Dimension;
import java.awt.Component;

import common.map.IMutableMapNG;
import common.map.IMapNG;
import common.map.Player;
import common.map.PlayerImpl;

import drivers.PlayerChangeMenuListener;
import drivers.IOHandler;

import common.map.fixtures.IResourcePile;
import common.map.fixtures.Implement;

import common.idreg.IDRegistrar;
import common.idreg.IDFactoryFiller;

import drivers.gui.common.about.AboutDialog;

import drivers.worker_mgmt.WorkerMenu;

import drivers.common.PlayerChangeListener;
import drivers.common.SPOptions;
import drivers.common.MultiMapGUIDriver;

import drivers.gui.common.MenuBroker;
import drivers.gui.common.SPFrame;
import drivers.gui.common.SPFileChooser;
import drivers.gui.common.WindowCloseListener;

import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import static lovelace.util.BoxPanel.centeredHorizontalBox;
import lovelace.util.ListenedButton;
import lovelace.util.StreamingLabel;
import lovelace.util.BoxPanel;
import lovelace.util.FileChooser;
import lovelace.util.FormattedLabel;
import static lovelace.util.BoxPanel.BoxAxis;
import lovelace.util.BorderedPanel;
import static lovelace.util.FunctionalSplitPane.verticalSplit;
import lovelace.util.MemoizedComboBox;
import lovelace.util.MissingFileException;
import lovelace.util.MalformedXMLException;

import java.math.BigDecimal;

import drivers.map_viewer.ViewerGUIFactory;

import java.util.Collections;

import common.xmlio.SPFormatException;

import java.io.IOException;
import java.io.FileNotFoundException;

/* package */ class ResourceAddingGUI implements MultiMapGUIDriver {
	private static final Logger LOGGER = Logger.getLogger(ResourceAddingGUI.class.getName());

	// TODO: Probably move inside frame class
	private static JPanel pairPanel(final Component first, final Component second) {
		return BorderedPanel.verticalPanel(first, null, second);
	}

	private static final String CSS = "color:black; margin-bottom: 0.5em; margin-top: 0.5em;";

	private static void logAddition(final StreamingLabel logLabel, final Player currentPlayer, final String addend) {
		logLabel.append(String.format("<p style=\"%s\">Added %s for %s</p>", CSS,
			addend, currentPlayer.getName()));
	}

	private static final String ERROR_CSS = "color:red; margin-bottom: 0.5em; margin-top: 0.5em;";

	private static Consumer<String> logError(final StreamingLabel logLabel) {
		return message -> logLabel.append(String.format("<p style=\"%s\">%s</p>",
			ERROR_CSS, message));
	}

	private static void addListenerToField(final JSpinner field, final ActionListener listener,
	                                       final StreamingLabel logLabel) {
		if (field.getEditor() instanceof JTextField) {
			((JTextField) field.getEditor()).addActionListener(listener);
		} else if (field.getEditor() instanceof JSpinner.DefaultEditor) {
			((JSpinner.DefaultEditor) field.getEditor()).getTextField()
				.addActionListener(listener);
		} else {
			logLabel.append("Spinner's editor wasn't a text field, but a ");
			logLabel.appendLine(field.getEditor().getClass().getName());
		}
	}


	private final ResourceManagementDriverModel model;

	@Override
	public ResourceManagementDriverModel getModel() {
		return model;
	}

	private final ICLIHelper cli;

	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	public ResourceAddingGUI(final ICLIHelper cli, final SPOptions options, final ResourceManagementDriverModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	/**
	 * A window to let the user enter resources etc. Note that this is not
	 * a dialog to enter one resource and close.
	 *
	 * TODO: Make dependencies explicit to make this static.
	 */
	private class ResourceAddingFrame extends SPFrame implements PlayerChangeListener {
		private final IDRegistrar idf;
		private Player currentPlayer;
		private final FormattedLabel resourceLabel;
		private final FormattedLabel implementLabel;
		private final StreamingLabel logLabel = new StreamingLabel();
		private final MemoizedComboBox resourceKindBox;

		private boolean playerIsDefault = true;

		private final BoxPanel mainPanel;

		private final ActionListener menuHandler;

		private final MemoizedComboBox resourceBox;
		private final MemoizedComboBox resourceUnitsBox;
		private final MemoizedComboBox implementKindBox;

		private final SpinnerNumberModel resourceQuantityModel;
		private final SpinnerNumberModel resourceCreatedModel;
		private final SpinnerNumberModel implementQuantityModel;

		private final JSpinner implementQuantityField;

		public ResourceAddingFrame(final ActionListener menuHandler, final MultiMapGUIDriver outer) {
			super("Resource Entry", outer, null, true);
			this.menuHandler = menuHandler;
			idf = IDFactoryFiller.createIDFactory(model.streamAllMaps()
					.toArray(IMapNG[]::new));
			currentPlayer = new PlayerImpl(-1, "");
			mainPanel = new BoxPanel(BoxAxis.PageAxis);

			resourceLabel = new FormattedLabel("Add equipment for %s:", currentPlayer.getName());
			mainPanel.add(resourceLabel);

			final JPanel resourcePanel = new BoxPanel(BoxAxis.LineAxis);
			resourceKindBox = new MemoizedComboBox(logError(logLabel));
			resourcePanel.add(pairPanel(new JLabel("General Category"), resourceKindBox));

			// If we set the maximum high at this point, the fields would try to be
			// unneccessarily large. I'm not sure that setting it low at first helps, though.
			resourceCreatedModel = new SpinnerNumberModel(-1, -1, 2000, 1);
			final JSpinner creationSpinner = new JSpinner(resourceCreatedModel);
			resourcePanel.add(pairPanel(new JLabel("Turn created"), creationSpinner));

			resourceBox = new MemoizedComboBox(logError(logLabel));
			resourcePanel.add(pairPanel(new JLabel("Specific Resource"), resourceBox));

			resourceQuantityModel = new SpinnerNumberModel(0, 0, 2000, 1);
			final JSpinner resourceQuantitySpinner = new JSpinner(resourceQuantityModel);
			resourcePanel.add(pairPanel(new JLabel("Quantity"), resourceQuantitySpinner));

			resourceUnitsBox = new MemoizedComboBox(logError(logLabel));
			resourcePanel.add(pairPanel(new JLabel("Units"), resourceUnitsBox));

			resourcePanel.add(pairPanel(new JLabel(""),
				new ListenedButton("Add Resource", this::resourceListener)));
			resourceUnitsBox.addSubmitListener(this::resourceListener);

			addListenerToField(creationSpinner, this::resourceListener, logLabel);
			addListenerToField(resourceQuantitySpinner, this::resourceListener, logLabel);

			resourceBox.addSubmitListener(this::resourceListener);
			resourceKindBox.addSubmitListener(this::resourceListener);
			mainPanel.add(resourcePanel);

			mainPanel.addGlue();
			implementLabel = new FormattedLabel("Add equipment for %s:",
				currentPlayer.getName());
			mainPanel.add(implementLabel);
			implementQuantityModel = new SpinnerNumberModel(1, 1, 2000, 1);
			implementQuantityField = new JSpinner(implementQuantityModel);
			implementKindBox = new MemoizedComboBox(logLabel::append);

			implementKindBox.addSubmitListener(this::implementListener);
			addListenerToField(implementQuantityField, this::implementListener, logLabel);

			mainPanel.add(centeredHorizontalBox(implementQuantityField,
				implementKindBox, new ListenedButton("Add Equipment",
				this::implementListener)));
			mainPanel.addGlue();
			final JScrollPane scrolledLog = new JScrollPane(logLabel);
			scrolledLog.setMinimumSize(logLabel.getMinimumSize());

			add(verticalSplit(mainPanel, scrolledLog, 0.2, 0.1));
			setJMenuBar(WorkerMenu.workerMenu(menuHandler, mainPanel, outer));
			pack();
			logLabel.setMinimumSize(new Dimension(getWidth() - 20, 50));
			logLabel.setPreferredSize(new Dimension(getWidth(), 100));

			final int maximum = Short.MAX_VALUE; // TODO: Get whatever runtime.maxArraySize was in Ceylon?

			resourceCreatedModel.setMaximum(maximum);
			resourceQuantityModel.setMaximum(maximum);
			implementQuantityModel.setMaximum(maximum);
		}

		private void confirmPlayer() {
			if (playerIsDefault && currentPlayer.getName().trim().isEmpty()) {
				menuHandler.actionPerformed(new ActionEvent(mainPanel, 1,
					"change current player"));
			}
			playerIsDefault = false;
		}

		private void resourceListener(final ActionEvent ignored) {
			confirmPlayer();
			final String kind = resourceKindBox.getSelectedString();
			final String resource = resourceBox.getSelectedString();
			final String units = resourceUnitsBox.getSelectedString();
			if (kind.isEmpty()) {
				resourceKindBox.requestFocusInWindow();
				return;
			} else if (resource.isEmpty()) {
				resourceBox.requestFocusInWindow();
				return;
			} else if (units.isEmpty()) {
				resourceUnitsBox.requestFocusInWindow();
				return;
			}

			final BigDecimal qty;
			try {
				// TODO: Can't we pass the number to BigDecimal directly?
				qty = new BigDecimal(resourceQuantityModel.getNumber().toString());
			} catch (final NumberFormatException except) {
				logLabel.appendLine("Failed to convert quantity into the form we need.");
				return;
			}
			final IResourcePile pile = model.addResourcePile(currentPlayer, idf.createID(),
				kind, resource, qty, units, resourceCreatedModel.getNumber().intValue());
			logAddition(logLabel, currentPlayer, pile.toString());
			resourceKindBox.checkAndClear();
			resourceBox.checkAndClear();
			resourceUnitsBox.checkAndClear();
			resourceCreatedModel.setValue(-1);
			resourceQuantityModel.setValue(0);
		}

		private void implementListener(final ActionEvent ignored) { // Param required for use in fields
			confirmPlayer();
			final String kind = implementKindBox.getSelectedString();
			if (kind.isEmpty()) {
				implementKindBox.requestFocusInWindow();
				return;
			}
			final int quantity = implementQuantityModel.getNumber().intValue();
			model.addResource(new Implement(kind, idf.createID(), quantity), currentPlayer);
			logAddition(logLabel, currentPlayer, String.format("%d x %s", quantity, kind));
			implementQuantityModel.setValue(1);
			implementKindBox.checkAndClear();
			implementQuantityField.requestFocusInWindow();
		}

		@Override
		public void playerChanged(@Nullable final Player old, final Player newPlayer) {
			currentPlayer = newPlayer;
			resourceLabel.setArguments(currentPlayer.getName());
			implementLabel.setArguments(currentPlayer.getName());
		}

		@Override
		public void acceptDroppedFile(final Path file) {
			try {
				model.addSubordinateMap(MapIOHelper.readMap(file));
			} catch (final SPFormatException except) {
				logError(logLabel).accept("SP map format error: " + except.getLocalizedMessage());
				LOGGER.log(Level.SEVERE, "SP map format error", except);
			} catch (final MissingFileException|NoSuchFileException|FileNotFoundException except) {
				logError(logLabel).accept(String.format("Dropped file %s couldn't be found", file));
				LOGGER.log(Level.SEVERE, "Dropped file couldn't be found", except);
			} catch (final IOException except) {
				logError(logLabel).accept("I/O error while reading dropped file: " + except.getLocalizedMessage());
				LOGGER.log(Level.SEVERE, "I/O error reading map file", except);
			} catch (final MalformedXMLException except) {
				logError(logLabel).accept(String.format("Dropped file %s contained malformed XML", file));
				LOGGER.log(Level.SEVERE, "Dropped file contained malformed XML", except);
			}
		}
	}

	private void startDriverImpl(final PlayerChangeMenuListener pcml, final MenuBroker menuHandler) {
		final ResourceAddingFrame frame = new ResourceAddingFrame(menuHandler, this);
		frame.addWindowListener(new WindowCloseListener(menuHandler));
		try {
			menuHandler.registerWindowShower(new AboutDialog(frame,
				frame.getWindowName()), "about");
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error loading about dialog text", except);
			// But the About dialog isn't critical, so go on ...
		}
		pcml.addPlayerChangeListener(frame);
		frame.showWindow();
	}

	@Override
	public void startDriver() {
		final PlayerChangeMenuListener pcml = new PlayerChangeMenuListener(model);
		final MenuBroker menuHandler = new MenuBroker();
		menuHandler.register(new IOHandler(this, cli),
			"load", "save", "save as", "new", "load secondary", "save all",
			"open in map viewer", "open secondary map in map viewer", "close", "quit");
		menuHandler.register(pcml, "change current player");
		SwingUtilities.invokeLater(() -> startDriverImpl(pcml, menuHandler));
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
			SwingUtilities.invokeLater(() -> new ResourceAddingGUI(cli, options,
				new ResourceManagementDriverModel(map)).startDriver());
		} else {
			model.setMap(map);
		}
	}
}

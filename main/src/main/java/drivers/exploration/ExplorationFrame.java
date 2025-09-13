package drivers.exploration;

import common.xmlio.SPFormatException;

import java.awt.Dimension;
import java.awt.GridLayout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.ComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import static lovelace.util.FunctionalSplitPane.horizontalSplit;

import javax.xml.stream.XMLStreamException;

import drivers.common.ISPDriver;
import drivers.gui.common.IMenuBroker;
import exploration.common.IExplorationModel;
import lovelace.util.BorderedPanel;
import lovelace.util.ListenedButton;
import goldberg.ImprovedComboBox;
import lovelace.util.FunctionalGroupLayout;
import lovelace.util.LovelaceLogger;
import lovelace.util.SimpleCardLayout;

import legacy.map.Player;

import legacy.map.fixtures.mobile.IUnit;

import drivers.gui.common.SPFrame;
import drivers.gui.common.SPMenu;

import exploration.common.Speed;

import legacy.xmlio.MapIOHelper;
import org.jspecify.annotations.Nullable;

/**
 * The main window for the exploration GUI.
 */
/* package */ final class ExplorationFrame extends SPFrame {
	@Serial
	private static final long serialVersionUID = 1L;
	private final SimpleCardLayout layoutObj;
	// "@Nullable Player" because IDEA has the idea that methods DOCUMENTED TO RETURN NULL
	// cannot return null.
	private final JList<@Nullable Player> playerList;
	private final IExplorationModel driverModel;
	private final UnitListModel unitListModel;
	private final JList<@Nullable IUnit> unitList;

	public ExplorationFrame(final IExplorationModel model, final Class<? extends IExplorationGUI> driverClass,
	                        final IMenuBroker menuHandler) {
		super("Exploration", model, new Dimension(768, 48), true);
		this.driverModel = model;

		layoutObj = new SimpleCardLayout(getContentPane());
		setLayout(layoutObj);

		final SpinnerNumberModel mpModel = new SpinnerNumberModel(0, 0, 2000, 0);
		final JSpinner mpField = new JSpinner(mpModel);

		unitListModel = new UnitListModel(model);
		unitList = new JList<>(unitListModel);

		final PlayerListModel playerListModel = new PlayerListModel(model);
		playerList = new JList<>(playerListModel);

		final ComboBoxModel<Speed> speedModel = new DefaultComboBoxModel<>(Speed.values());
		model.addMapChangeListener(playerListModel);

		playerList.addListSelectionListener(ignored -> handlePlayerChanged());
		menuHandler.register(ignored -> handlePlayerChanged(), "change current player");

		unitList.setCellRenderer(new UnitCellRenderer());

		speedModel.setSelectedItem(Speed.Normal);

		final JPanel tilesPanel = new JPanel(new GridLayout(3, 12, 2, 2));

		final JPanel headerPanel = new JPanel();
		final FunctionalGroupLayout headerLayout = new FunctionalGroupLayout(headerPanel);

		final ExplorationPanel explorationPanel = new ExplorationPanel(mpModel, speedModel, headerPanel,
				headerLayout, tilesPanel, model, layoutObj::goNext);

		model.addSelectionChangeListener(explorationPanel);

		if (mpField.getEditor() instanceof final JTextField tf) {
			tf.addActionListener(ignored -> buttonListener());
		}

		add(new BorderedPanel(horizontalSplit(
				BorderedPanel.verticalPanel(new JLabel("Players in all maps:"), playerList, null),
				BorderedPanel.verticalPanel(new JLabel("<html><body><p>Units belonging to that player:</p>" +
								"<p>(Selected unit will be used for exploration.)</p></body></html>"),
						new JScrollPane(unitList), BorderedPanel.verticalPanel(
								BorderedPanel.horizontalPanel(new JLabel("Unit's Movement Points"), null, mpField),
								BorderedPanel.horizontalPanel(new JLabel("Unit's Relative Speed"),
										null, new ImprovedComboBox<>(speedModel)),
								new ListenedButton("Start exploring!", this::buttonListener))))));

		add(explorationPanel);

		setPreferredSize(new Dimension(1024, 640));

		setJMenuBar(SPMenu.forWindowContaining(explorationPanel,
				SPMenu.createFileMenu(menuHandler, driverClass),
				SPMenu.disabledMenu(SPMenu.createMapMenu(menuHandler, driverClass)),
				SPMenu.createViewMenu(menuHandler, driverClass)));
		pack();
	}

	@Override
	public void acceptDroppedFile(final Path file) {
		try {
			driverModel.addSubordinateMap(MapIOHelper.readMap(file));
			// FIXME: THrow DriverFailedException and/or show error dialog on error
		} catch (final SPFormatException except) {
			LovelaceLogger.error(except, "SP format error in dropped file %s", file);
		} catch (final NoSuchFileException | FileNotFoundException except) {
			LovelaceLogger.error(except, "Dropped file not found: %s", file);
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LovelaceLogger.error(except, "I/O error reading dropped file");
		} catch (final XMLStreamException except) {
			LovelaceLogger.error(except, "Malformed XML in dropped file");
		}
	}

	private void handlePlayerChanged() {
		layoutObj.goFirst();
		if (!playerList.isSelectionEmpty()) {
			final Player newPlayer = playerList.getSelectedValue();
			// JList::getSelectedValue is documented TO RETURN NULL in some cases!
			//noinspection ConstantExpression
			if (Objects.nonNull(newPlayer)) {
				unitListModel.playerChanged(null, newPlayer);
			}
		}
	}

	private void buttonListener() {
		LovelaceLogger.trace("In ExplorationFrame.buttonListener");
		final IUnit selectedValue = unitList.getSelectedValue();
		// JList::getSelectedValue is documented TO RETURN NULL in some cases!
		//noinspection ConstantExpression
		if (Objects.nonNull(selectedValue) && !unitList.isSelectionEmpty()) {
			driverModel.setSelectedUnit(selectedValue);
			LovelaceLogger.trace("ExplorationFrame.buttonListener: after selectedUnit setter call");
			layoutObj.goNext();
		} else {
			LovelaceLogger.warning("Apparently no unit selected");
		}
		LovelaceLogger.trace("End of ExplorationFrame.buttonListener");
	}
}

package view.exploration;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.Document;

import org.eclipse.jdt.annotation.Nullable;

import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.listeners.MovementCostListener;
import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSupport;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.Point;
import util.IsNumeric;
import util.NullCleaner;
import util.Pair;
import view.map.details.FixtureList;
import view.util.BorderedPanel;
import view.util.BoxPanel;
import view.util.ListenedButton;

/**
 * A panel to let the user explore using a unit.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ExplorationPanel extends BorderedPanel implements ActionListener,
		SelectionChangeListener, CompletionSource, MovementCostListener {
	/**
	 * The label showing the current location of the explorer.
	 */
	private final JLabel locLabel = new JLabel(
			"<html><body>Currently exploring (-1, -1); click a tile to explore it. "
					+ "Selected fixtures in its left-hand list will be 'discovered'."
					+ "</body></html>");
	/**
	 * The list of completion listeners listening to us.
	 */
	private final Collection<CompletionListener> cListeners = new ArrayList<>();

	/**
	 * The text-field containing the running MP total.
	 */
	private final JTextField mpField;
	/**
	 * The collection of proxies for main-map tile-fixture-lists.
	 */
	private final Map<Direction, SelectionChangeSupport> mains = new EnumMap<>(
			Direction.class);
	/**
	 * The collection of proxies for secondary-map tile-fixture lists.
	 */
	private final Map<Direction, SelectionChangeSupport> seconds = new EnumMap<>(
			Direction.class);
	/**
	 * The collection of dual-tile-buttons.
	 */
	private final Map<Direction, DualTileButton> buttons = new EnumMap<>(
			Direction.class);
	/**
	 * Key-bindings for dual-tile buttons: arrow keys.
	 */
	private static final Map<Direction, KeyStroke> ARROW_KEYS = new EnumMap<>(
			Direction.class);
	/**
	 * Key bindings for dual-tile buttons: numeric keypad.
	 */
	private static final Map<Direction, KeyStroke> NUM_KPAD = new EnumMap<>(
			Direction.class);
	static {
		ARROW_KEYS.put(Direction.North, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));
		ARROW_KEYS.put(Direction.South, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
		ARROW_KEYS.put(Direction.West, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)));
		ARROW_KEYS.put(Direction.East, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)));
		NUM_KPAD.put(Direction.North, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0)));
		NUM_KPAD.put(Direction.South, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0)));
		NUM_KPAD.put(Direction.West, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0)));
		NUM_KPAD.put(Direction.East, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0)));
		NUM_KPAD.put(Direction.Northeast, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0)));
		NUM_KPAD.put(Direction.Northwest, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0)));
		NUM_KPAD.put(Direction.Southeast, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0)));
		NUM_KPAD.put(Direction.Southwest, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0)));
		NUM_KPAD.put(Direction.Nowhere, NullCleaner
				.assertNotNull(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0)));
	}
	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;
	/**
	 * The text for the 'back' button.
	 */
	private static final String BACK_TEXT = "Select a different explorer";

	/**
	 * Constructor.
	 *
	 * @param emodel the exploration model.
	 * @param mpDoc the model underlying the remaining-MP text boxes.
	 */
	public ExplorationPanel(final IExplorationModel emodel, final Document mpDoc) {
		model = emodel;
		final BoxPanel headerPanel = new BoxPanel(true);
		headerPanel.add(new ListenedButton(BACK_TEXT, this));
		headerPanel.add(locLabel);
		headerPanel.add(new JLabel("Remaining Movement Points: "));
		mpField = new JTextField(mpDoc, null, 5);
		// TODO: store reference to document, not text field, in class body
		headerPanel.add(mpField);
		setCenter(new JSplitPane(JSplitPane.VERTICAL_SPLIT, headerPanel,
				setupTilesGUI(new JPanel(new GridLayout(3, 12, 2, 2)))));
	}

	/**
	 * Set up the GUI for the surrounding tiles.
	 *
	 * @param panel the panel to add them all to.
	 * @return it
	 */
	private JPanel setupTilesGUI(final JPanel panel) {
		return setupTilesGUIImpl(panel, Direction.Northwest, Direction.North,
				Direction.Northeast, Direction.West, Direction.Nowhere,
				Direction.East, Direction.Southwest, Direction.South,
				Direction.Southeast);
	}

	/**
	 * Set up the GUI for multiple tiles.
	 *
	 * @param panel the panel to add them all to.
	 * @param directions the directions to create GUIs for
	 * @return the panel
	 */
	private JPanel setupTilesGUIImpl(final JPanel panel,
			final Direction... directions) {
		for (final Direction direction : directions) {
			if (direction != null) {
				addTileGUI(panel, direction);
			}
		}
		return panel;
	}

	/**
	 * Handle a button press.
	 *
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt != null && BACK_TEXT.equalsIgnoreCase(evt.getActionCommand())) {
			for (final CompletionListener list : cListeners) {
				list.stopWaitingOn(true);
			}
		}
	}

	/**
	 * Set up the GUI representation of a tile---a list of its contents in the
	 * main map, a visual representation, and a list of its contents in a
	 * secondary map.
	 *
	 * @param panel the panel to add them to
	 * @param direction which direction from the currently selected tile this
	 *        GUI represents.
	 */
	private void addTileGUI(final JPanel panel, final Direction direction) {
		final SelectionChangeSupport mainPCS = new SelectionChangeSupport();
		final FixtureList mainList = new FixtureList(panel, model, model.getMap()
				.players());
		mainPCS.addSelectionChangeListener(mainList);
		panel.add(new JScrollPane(mainList));
		final DualTileButton dtb =
				new DualTileButton(model.getMap(), model.getSubordinateMaps()
						.iterator().next().first());
		// At some point we tried wrapping the button in a JScrollPane.
		panel.add(dtb);
		final ExplorationClickListener ecl = new ExplorationClickListener(
				model, direction, mainList);
		dtb.addActionListener(ecl);
		final InputMap dtbIMap = dtb.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		dtbIMap.put(ARROW_KEYS.get(direction), direction.toString());
		dtbIMap.put(NUM_KPAD.get(direction), direction.toString());
		dtb.getActionMap().put(direction.toString(), ecl);
		ecl.addSelectionChangeListener(this);
		ecl.addMovementCostListener(this);
		mainList.getModel().addListDataListener(
				new ExplorationListListener(model, mainList));
		final SelectionChangeSupport secPCS = new SelectionChangeSupport();
		final Iterator<Pair<IMutableMapNG, File>> subMaps =
				model.getSubordinateMaps().iterator();
		final Iterable<Player> players;
		if (subMaps.hasNext()) {
			players = subMaps.next().first().players();
		} else {
			players = model.getMap().players();
		}
		final FixtureList secList = new FixtureList(panel, model, players);
		secPCS.addSelectionChangeListener(secList);
		panel.add(new JScrollPane(secList));
		mains.put(direction, mainPCS);
		buttons.put(direction, dtb);
		seconds.put(direction, secPCS);
	}
	/**
	 * A parser for numeric input.
	 */
	private static final NumberFormat NUM_PARSER = NullCleaner
			.assertNotNull(NumberFormat.getIntegerInstance());
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = NullCleaner.assertNotNull(Logger
			.getLogger(ExplorationPanel.class.getName()));

	/**
	 * Account for a movement cost.
	 *
	 * @param cost how much the movement cost
	 */
	@Override
	public void deduct(final int cost) {
		final String mpText = mpField.getText().trim();
		if (mpText != null && IsNumeric.isNumeric(mpText)) {
			int mpoints;
			try {
				mpoints = NUM_PARSER.parse(mpText).intValue();
			} catch (final ParseException e) {
				LOGGER.log(Level.SEVERE,
						"Non-numeic data in movement-points field", e);
				return;
			}
			mpoints -= cost;
			mpField.setText(Integer.toString(mpoints));
		}
	}

	/**
	 * @param old the previously selected location
	 * @param newPoint the newly selected location
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old,
			final Point newPoint) {
		final Point selPoint = model.getSelectedUnitLocation();
		for (final Direction dir : Direction.values()) {
			assert dir != null;
			final Point point = model.getDestination(selPoint, dir);
			mains.get(dir).fireChanges(selPoint, point);
			seconds.get(dir).fireChanges(selPoint, point);
			buttons.get(dir).setPoint(point);
			buttons.get(dir).repaint();
		}
		locLabel.setText("<html><body>Currently exploring "
				+ model.getSelectedUnitLocation()
				+ "; click a tile to explore it. "
				+ "Selected fixtures in its left-hand list "
				+ "will be 'discovered'.</body></html>");
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addCompletionListener(final CompletionListener list) {
		cListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeCompletionListener(final CompletionListener list) {
		cListeners.remove(list);
	}
}

package view.exploration;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import model.exploration.IExplorationModel;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.listeners.MovementCostListener;
import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSupport;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.Point;
import org.eclipse.jdt.annotation.Nullable;
import util.IsNumeric;
import util.NullCleaner;
import util.Pair;
import view.map.details.FixtureList;
import view.util.BorderedPanel;
import view.util.BoxPanel;
import view.util.ListenedButton;
import view.util.SplitWithWeights;

/**
 * A panel to let the user explore using a unit.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ExplorationPanel extends BorderedPanel
		implements SelectionChangeListener, CompletionSource, MovementCostListener {
	/**
	 * The label showing the current location of the explorer.
	 */
	private final JLabel locLabel = new JLabel("<html><body>Currently exploring (-1, " +
													"-1); click a tile to explore it." +
													" Selected fixtures in its left-hand" +
													" list will be 'discovered'." +
													"</body></html>");
	/**
	 * The list of completion listeners listening to us.
	 */
	private final Collection<CompletionListener> cListeners = new ArrayList<>();

	/**
	 * The text-field containing the running MP total.
	 */
	private final Document mpDocument;
	/**
	 * The collection of proxies for main-map tile-fixture-lists.
	 */
	private final Map<IExplorationModel.Direction, SelectionChangeSupport> mains =
			new EnumMap<>(IExplorationModel.Direction.class);
	/**
	 * The collection of proxies for secondary-map tile-fixture lists.
	 */
	private final Map<IExplorationModel.Direction, SelectionChangeSupport> seconds =
			new EnumMap<>(IExplorationModel.Direction.class);
	/**
	 * The collection of dual-tile-buttons.
	 */
	private final Map<IExplorationModel.Direction, DualTileButton> buttons =
			new EnumMap<>(IExplorationModel.Direction.class);

	/**
	 * @param direction a direction
	 * @return the corresponding arrow key, or null if not supported
	 */
	@SuppressWarnings("EnumSwitchStatementWhichMissesCases")
	@Nullable
	private static KeyStroke getArrowKey(final IExplorationModel.Direction direction) {
		switch (direction) {
		case North:
			return KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
		case South:
			return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		case West:
			return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		case East:
			return KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		default:
			return null;
		}
	}

	/**
	 * @param direction a direction
	 * @return the corresponding numeric-keypad key, or null if not supported
	 */
	@Nullable
	private static KeyStroke getNumpadKey(final IExplorationModel.Direction direction) {
		switch (direction) {
		case North:
			return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0);
		case South:
			return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0);
		case West:
			return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0);
		case East:
			return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0);
		case Northeast:
			return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0);
		case Northwest:
			return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0);
		case Southeast:
			return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0);
		case Southwest:
			return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0);
		case Nowhere:
			return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0);
		default:
			return null;
		}
	}

	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;

	/**
	 * Constructor.
	 *
	 * @param explorationModel the exploration model.
	 * @param mpDoc  the model underlying the remaining-MP text boxes.
	 */
	public ExplorationPanel(final IExplorationModel explorationModel,
							final Document mpDoc) {
		model = explorationModel;
		final JPanel headerPanel = new BoxPanel(true);
		headerPanel.add(new ListenedButton("Select a different explorer", evt -> {
			for (final CompletionListener list : cListeners) {
				list.finished();
			}
		}));
		headerPanel.add(locLabel);
		headerPanel.add(new JLabel("Remaining Movement Points: "));
		mpDocument = mpDoc;
		headerPanel.add(new JTextField(mpDocument, null, 5));
		setCenter(SplitWithWeights.verticalSplit(0.5, 0.5, headerPanel,
				setupTilesGUI(new JPanel(new GridLayout(3, 12, 2, 2)))));
	}

	/**
	 * Set up the GUI for the surrounding tiles.
	 *
	 * @param panel the panel to add them all to.
	 * @return it
	 */
	private JPanel setupTilesGUI(final JPanel panel) {
		return setupTilesGUIImpl(panel, IExplorationModel.Direction.Northwest,
				IExplorationModel.Direction.North, IExplorationModel.Direction.Northeast,
				IExplorationModel.Direction.West, IExplorationModel.Direction.Nowhere,
				IExplorationModel.Direction.East, IExplorationModel.Direction.Southwest,
				IExplorationModel.Direction.South, IExplorationModel.Direction.Southeast);
	}

	/**
	 * Set up the GUI for multiple tiles.
	 *
	 * @param panel      the panel to add them all to.
	 * @param directions the directions to create GUIs for
	 * @return the panel
	 */
	private JPanel setupTilesGUIImpl(final JPanel panel,
									 final IExplorationModel.Direction... directions) {
		for (final IExplorationModel.Direction direction : directions) {
			if (direction != null) {
				addTileGUI(panel, direction);
			}
		}
		return panel;
	}

	/**
	 * Set up the GUI representation of a tile---a list of its contents in the main
	 * map, a
	 * visual representation, and a list of its contents in a secondary map.
	 *
	 * @param panel     the panel to add them to
	 * @param direction which direction from the currently selected tile this GUI
	 *                  represents.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void addTileGUI(final JPanel panel,
							final IExplorationModel.Direction direction) {
		final SelectionChangeSupport mainPCS = new SelectionChangeSupport();
		final FixtureList mainList =
				new FixtureList(panel, model, model.getMap().players());
		mainPCS.addSelectionChangeListener(mainList);
		panel.add(new JScrollPane(mainList));
		final DualTileButton dtb =
				new DualTileButton(model.getMap(),
										model.getSubordinateMaps().iterator().next()
												.first());
		// At some point we tried wrapping the button in a JScrollPane.
		panel.add(dtb);
		final ExplorationClickListener ecl =
				new ExplorationClickListener(model, direction, mainList);
		dtb.addActionListener(ecl);
		final InputMap dtbIMap = dtb.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		final KeyStroke arrowKey = getArrowKey(direction);
		if (arrowKey != null) {
			dtbIMap.put(arrowKey, direction.toString());
		}
		final KeyStroke numpadKey = getNumpadKey(direction);
		if (numpadKey != null) {
			dtbIMap.put(numpadKey, direction.toString());
		}
		dtb.getActionMap().put(direction.toString(), ecl);
		ecl.addSelectionChangeListener(this);
		ecl.addMovementCostListener(this);
		mainList.getModel().addListDataListener(
				new ExplorationListListener(model, mainList));
		final Iterator<Pair<IMutableMapNG, Optional<Path>>> subMaps =
				model.getSubordinateMaps().iterator();
		final Iterable<Player> players;
		if (subMaps.hasNext()) {
			players = subMaps.next().first().players();
		} else {
			players = model.getMap().players();
		}
		final FixtureList secList = new FixtureList(panel, model, players);
		final SelectionChangeSupport secPCS = new SelectionChangeSupport();
		secPCS.addSelectionChangeListener(secList);
		panel.add(new JScrollPane(secList));
		mains.put(direction, mainPCS);
		buttons.put(direction, dtb);
		seconds.put(direction, secPCS);
	}

	/**
	 * A parser for numeric input.
	 */
	private static final NumberFormat NUM_PARSER =
			NullCleaner.assertNotNull(NumberFormat.getIntegerInstance());
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			NullCleaner.assertNotNull(Logger.getLogger(ExplorationPanel.class.getName()));

	/**
	 * Account for a movement cost.
	 *
	 * @param cost how much the movement cost
	 */
	@Override
	public void deduct(final int cost) {
		final String mpText;
		try {
			mpText = NullCleaner.assertNotNull(
					mpDocument.getText(0, mpDocument.getLength()).trim());
		} catch (final BadLocationException except) {
			LOGGER.log(Level.SEVERE, "Exception trying to update MP counter", except);
			return;
		}
		if (IsNumeric.isNumeric(mpText)) {
			int movePoints;
			try {
				movePoints = NUM_PARSER.parse(mpText).intValue();
			} catch (final ParseException e) {
				LOGGER.log(Level.SEVERE,
						"Non-numeric data in movement-points field", e);
				return;
			}
			movePoints -= cost;
			try {
				mpDocument.remove(0, mpDocument.getLength());
				mpDocument.insertString(0, Integer.toString(movePoints), null);
			} catch (final BadLocationException except) {
				LOGGER.log(Level.SEVERE, "Exception trying to update MP counter", except);
			}
		}
	}

	/**
	 * @param old      the previously selected location
	 * @param newPoint the newly selected location
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		final Point selPoint = model.getSelectedUnitLocation();
		for (final IExplorationModel.Direction dir : IExplorationModel.Direction.values()) {
			assert dir != null;
			final Point point = model.getDestination(selPoint, dir);
			NullCleaner.assertNotNull(mains.get(dir)).fireChanges(selPoint, point);
			NullCleaner.assertNotNull(seconds.get(dir)).fireChanges(selPoint, point);
			NullCleaner.assertNotNull(buttons.get(dir)).setPoint(point);
			NullCleaner.assertNotNull(buttons.get(dir)).repaint();
		}
		locLabel.setText(
				"<html><body>Currently exploring " + model.getSelectedUnitLocation() +
						"; click a tile to explore it. Selected fixtures in its " +
						"left-hand list will be 'discovered'.</body></html>");
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
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * @return a diagnostic String
	 */
	@Override
	public String toString() {
		try {
			return "ExplorationPanel with remaining MP: " +
						mpDocument.getText(0, mpDocument.getLength());
		} catch (final BadLocationException ignored) {
			return "ExplorationPanel";
		}
	}
}

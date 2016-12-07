package view.exploration;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.ObjIntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.listeners.MovementCostListener;
import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSupport;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.viewer.FixtureFilterTableModel;
import model.viewer.FixtureListModel;
import model.viewer.FixtureMatcher;
import org.eclipse.jdt.annotation.Nullable;
import util.IsNumeric;
import util.NullCleaner;
import util.Pair;
import view.map.details.FixtureList;
import view.util.BorderedPanel;
import view.util.BoxPanel;
import view.util.FormattedLabel;
import view.util.ListenedButton;
import view.util.SplitWithWeights;

/**
 * A panel to let the user explore using a unit.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ExplorationPanel extends BorderedPanel
		implements SelectionChangeListener, CompletionSource, MovementCostListener {
	/**
	 * A parser for numeric input.
	 */
	private static final NumberFormat NUM_PARSER =
			NullCleaner.assertNotNull(NumberFormat.getIntegerInstance());
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			NullCleaner.assertNotNull(Logger.getLogger(ExplorationPanel.class.getName
																					  ()));
	/**
	 * The mapping from directions to arrow keys.
	 */
	private static final Map<IExplorationModel.Direction, KeyStroke> ARROW_KEYS =
			new EnumMap<>(IExplorationModel.Direction.class);
	/**
	 * The mapping from directions to numeric-keypad keys.
	 */
	private static final Map<IExplorationModel.Direction, KeyStroke> NUM_KEYS =
			new EnumMap<>(IExplorationModel.Direction.class);
	/**
	 * The label showing the current location of the explorer.
	 */
	private final FormattedLabel locLabel =
			new FormattedLabel("<html><body>Currently exploring (%d, %d); click a tile " +
									   "to explore it. Selected fixtures in its " +
									   "left-hand list will be 'discovered'" +
									   ".</body></html>",
									  Integer.valueOf(-1), Integer.valueOf(-1));
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
	 * A source of matchers to determine which fixtures to draw on top.
	 */
	private final Iterable<FixtureMatcher> matchers = new FixtureFilterTableModel();
	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;

	static {
		ObjIntConsumer<IExplorationModel.Direction> arrow =
				(dir, num) -> ARROW_KEYS.put(dir, KeyStroke.getKeyStroke(num, 0));
		arrow.accept(Direction.North, KeyEvent.VK_UP);
		arrow.accept(Direction.South, KeyEvent.VK_DOWN);
		arrow.accept(Direction.West, KeyEvent.VK_LEFT);
		arrow.accept(Direction.East, KeyEvent.VK_RIGHT);
		ObjIntConsumer<IExplorationModel.Direction> numPad =
				(dir, num) -> NUM_KEYS.put(dir, KeyStroke.getKeyStroke(num, 0));
		numPad.accept(Direction.North, KeyEvent.VK_NUMPAD8);
		numPad.accept(Direction.South, KeyEvent.VK_NUMPAD2);
		numPad.accept(Direction.West, KeyEvent.VK_NUMPAD4);
		numPad.accept(Direction.East, KeyEvent.VK_NUMPAD6);
		numPad.accept(Direction.Northeast, KeyEvent.VK_NUMPAD9);
		numPad.accept(Direction.Northwest, KeyEvent.VK_NUMPAD7);
		numPad.accept(Direction.Southeast, KeyEvent.VK_NUMPAD3);
		numPad.accept(Direction.Southwest, KeyEvent.VK_NUMPAD1);
		numPad.accept(Direction.Nowhere, KeyEvent.VK_NUMPAD5);
	}
	/**
	 * Constructor.
	 *
	 * @param explorationModel the exploration model.
	 * @param mpDoc            the model underlying the remaining-MP text boxes.
	 */
	public ExplorationPanel(final IExplorationModel explorationModel,
							final Document mpDoc) {
		model = explorationModel;
		final JPanel headerPanel = new BoxPanel(true);
		headerPanel.add(new ListenedButton("Select a different explorer",
												  evt -> cListeners.forEach(
														  CompletionListener::finished)));
		headerPanel.add(locLabel);
		headerPanel.add(new JLabel("Remaining Movement Points: "));
		mpDocument = mpDoc;
		final JPanel mpPanel = new JPanel();
		mpPanel.add(new JTextField(mpDocument, null, 5));
		headerPanel.add(mpPanel);
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
				IExplorationModel.Direction.South, IExplorationModel.Direction
														   .Southeast);
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
		final IMapNG secondMap =
				StreamSupport.stream(model.getSubordinateMaps().spliterator(), false)
						.map(Pair::first).findFirst().orElseGet(model::getMap);
		for (final IExplorationModel.Direction direction : directions) {
			if (direction != null) {
				addTileGUI(panel, secondMap, direction);
			}
		}
		return panel;
	}

	/**
	 * Set up the GUI representation of a tile---a list of its contents in the main
	 * map, a visual representation, and a list of its contents in a secondary map.
	 *
	 * @param panel          the panel to add them to
	 * @param subordinateMap the map to use for the subordinate map.
	 * @param direction      which direction from the currently selected tile this GUI
	 *                       represents.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void addTileGUI(final JPanel panel, final IMapNG subordinateMap,
							final IExplorationModel.Direction direction) {
		final SelectionChangeSupport mainPCS = new SelectionChangeSupport();
		final FixtureList mainList =
				new FixtureList(panel, new FixtureListModel(model),
									   model.getMap().players());
		mainPCS.addSelectionChangeListener(mainList);
		panel.add(new JScrollPane(mainList));
		final DualTileButton dtb =
				new DualTileButton(model.getMap(), subordinateMap, matchers);
		// At some point we tried wrapping the button in a JScrollPane.
		panel.add(dtb);
		final ExplorationClickListener ecl =
				new ExplorationClickListener(model, direction, mainList);
		dtb.addActionListener(ecl);
		final InputMap dtbIMap = dtb.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		if (ARROW_KEYS.containsKey(direction)) {
			dtbIMap.put(ARROW_KEYS.get(direction), direction.toString());
		}
		if (NUM_KEYS.containsKey(direction)) {
			dtbIMap.put(NUM_KEYS.get(direction), direction.toString());
		}
		dtb.getActionMap().put(direction.toString(), ecl);
		ecl.addSelectionChangeListener(this);
		ecl.addMovementCostListener(this);
		final SelectionChangeListener ell = new ExplorationListListener(model, mainList);
//		mainList.getModel().addListDataListener(ell);
		model.addSelectionChangeListener(ell);
		ecl.addSelectionChangeListener(ell);
		final Optional<Iterable<Player>> subMapPlayers =
				StreamSupport.stream(model.getSubordinateMaps().spliterator(), false)
						.map(Pair::first).map(IMapNG::players).findFirst();
		final Iterable<Player> players;
		players = subMapPlayers.orElseGet(() -> model.getMap().players());
		final FixtureList secList =
				new FixtureList(panel, new FixtureListModel(model), players);
		final SelectionChangeSupport secPCS = new SelectionChangeSupport();
		secPCS.addSelectionChangeListener(secList);
		panel.add(new JScrollPane(secList));
		mains.put(direction, mainPCS);
		buttons.put(direction, dtb);
		seconds.put(direction, secPCS);
		ell.selectedPointChanged(null, model.getSelectedUnitLocation());
	}

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
				LOGGER.log(Level.SEVERE, "Exception trying to update MP counter",
						except);
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
		for (final IExplorationModel.Direction dir : IExplorationModel.Direction
															 .values()) {
			assert dir != null;
			final Point point = model.getDestination(selPoint, dir);
			NullCleaner.assertNotNull(mains.get(dir)).fireChanges(selPoint, point);
			NullCleaner.assertNotNull(seconds.get(dir)).fireChanges(selPoint, point);
			NullCleaner.assertNotNull(buttons.get(dir)).setPoint(point);
			NullCleaner.assertNotNull(buttons.get(dir)).repaint();
		}
		locLabel.setArgs(Integer.valueOf(selPoint.getRow()),
				Integer.valueOf(selPoint.getCol()));
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
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({"unused", "static-method"})
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

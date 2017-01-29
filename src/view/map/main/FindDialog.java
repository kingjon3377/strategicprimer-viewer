package view.map.main;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import model.map.FixtureIterable;
import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.RiverFixture;
import model.misc.IDriverModel;
import model.viewer.IViewerModel;
import model.viewer.PointIterator;
import model.viewer.ZOrderFilter;
import org.eclipse.jdt.annotation.Nullable;
import util.IsNumeric;
import util.OnMac;
import view.util.BoxPanel;
import view.util.ListenedButton;
import view.util.SPDialog;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static view.util.BorderedPanel.verticalPanel;
import static view.util.SplitWithWeights.horizontalSplit;
import static view.util.SystemOut.SYS_OUT;

/**
 * A dialog to let the user find fixtures by ID, name, or "kind".
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
public final class FindDialog extends SPDialog {
	/**
	 * The proportion between the bulk of the dialog and the filter list.
	 */
	private static final double FILTER_PROPORTION = 0.6;
	/**
	 * A parser to convert from strings to integers.
	 */
	private static final NumberFormat NUM_PARSER = NumberFormat.getIntegerInstance();
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(FindDialog.class.getName());
	/**
	 * The text field holding the search string.
	 */
	private final JTextField search = new JTextField("", 20);
	/**
	 * The map model to change the selection in.
	 */
	private final IViewerModel map;
	/**
	 * The checkbox for searching backwards.
	 */
	private final JCheckBox backwards = new JCheckBox("Search backwards");
	/**
	 * The checkbox for searching vertically.
	 */
	private final JCheckBox vertically =
			new JCheckBox("Search vertically then horizontally");
	/**
	 * The checkbox for making case-sensitive search.
	 */
	private final JCheckBox caseSensitive = new JCheckBox("Case-sensitive search");
	/**
	 * The filter, to let the user filter which fixtures are displayed.
	 */
	private final FixtureFilterList ffl;
	/**
	 * Constructor.
	 *
	 * @param parent the parent to attach this dialog to
	 * @param model  the map model to change the selection in
	 */
	public FindDialog(final Frame parent, final IViewerModel model) {
		super(parent, "Find");
		@SuppressWarnings("UnnecessaryLocalVariable")
		final Frame parentFrame = parent;

		final ActionListener okListener = evt -> {
			search();
			setVisible(false);
			parentFrame.requestFocus();
			dispose();
		};

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		search.addActionListener(okListener);
		search.setActionCommand("OK");

		final JPanel searchBoxPane = new JPanel();
		searchBoxPane.add(search);
		final JPanel contentPane = new BoxPanel(false);
		contentPane.add(searchBoxPane);
		contentPane.add(backwards);
		contentPane.add(vertically);
		contentPane.add(caseSensitive);

		final BoxPanel buttonPanel = new BoxPanel(true);
		buttonPanel.addGlue();
		final ListenedButton okButton = new ListenedButton("OK", okListener);
		final ListenedButton cancelButton = new ListenedButton("Cancel", evt -> {
			setVisible(false);
			parentFrame.requestFocus();
			dispose();
		});
		OnMac.makeButtonsSegmented(okButton, cancelButton);
		if (OnMac.SYSTEM_IS_MAC) {
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			search.putClientProperty("JTextField.variant", "search");
			search.putClientProperty("JTextField.Search.FindAction", okListener);
			final ActionListener clearListener = evt -> search.setText("");
			search.putClientProperty("JTextField.Search.CancelAction", clearListener);
		} else {
			buttonPanel.add(okButton);
			buttonPanel.addGlue();
			buttonPanel.add(cancelButton);
		}
		buttonPanel.addGlue();
		contentPane.add(buttonPanel);
		ffl = new FixtureFilterList();
		SwingUtilities.invokeLater(new FilterPopulator(ffl, model));
		setContentPane(horizontalSplit(FILTER_PROPORTION, FILTER_PROPORTION,
				contentPane, verticalPanel(new JLabel("Find only ..."),
						new JScrollPane(ffl, VERTICAL_SCROLLBAR_AS_NEEDED,
											   HORIZONTAL_SCROLLBAR_NEVER), null)));
		map = model;
		pack();
	}

	/**
	 * Whether the fixture has an owner matching the given pattern.
	 * @param pattern         a pattern
	 * @param idNum           the ID number that is the pattern if the pattern is numeric
	 * @param fix             a fixture
	 * @param caseSensitivity whether to search case-sensitively
	 * @return whether the fixture has an owner that matches the pattern.
	 */
	private static boolean matchesOwner(final String pattern, final int idNum,
										final IFixture fix, final boolean
																	caseSensitivity) {
		if (fix instanceof HasOwner) {
			final Player owner = ((HasOwner) fix).getOwner();
			final String ownerName;
			if (caseSensitivity) {
				ownerName = owner.getName();
			} else {
				ownerName = owner.getName().toLowerCase();
			}
			return (owner.getPlayerId() == idNum) || ownerName.contains(pattern) ||
						   matchesOwnerSpecials(pattern, owner);
		} else {
			return false;
		}
	}

	/**
	 * Whether the fixture has an owner matching the given pattern in our special cases.
	 * @param player  the player owning a fixture
	 * @param pattern a pattern
	 * @return whether the pattern is "me" and the fixture is owned by the current
	 * player, or if the pattern is "none" and the fixture is independent.
	 */
	private static boolean matchesOwnerSpecials(final String pattern,
												final Player player) {
		return ("me".equalsIgnoreCase(pattern.trim()) && player.isCurrent()) ||
					   ("none".equalsIgnoreCase(pattern.trim()) &&
								player.isIndependent());
	}

	/**
	 * Whether the fixture has a "kind" matching the given pattern.
	 * @param pattern         a pattern
	 * @param fix             a fixture
	 * @param caseSensitivity whether to search case-sensitively
	 * @return whether the fixture has a 'kind' that matches the pattern
	 */
	private static boolean matchesKind(@SuppressWarnings("TypeMayBeWeakened")
									   final String pattern, final IFixture fix,
									   final boolean caseSensitivity) {
		if (caseSensitivity) {
			return (fix instanceof HasKind) &&
						   ((HasKind) fix).getKind().contains(pattern);
		} else {
			return (fix instanceof HasKind)
						   && ((HasKind) fix).getKind().toLowerCase()
									  .contains(pattern);
		}
	}

	/**
	 * Whether the fixture has a name matching the given pattern.
	 * @param pattern         a patter
	 * @param fix             a fixture
	 * @param caseSensitivity whether to search case-sensitively
	 * @return whether the fixture has a name that matches the pattern
	 */
	private static boolean matchesName(@SuppressWarnings("TypeMayBeWeakened") final
									   String pattern,
									   final IFixture fix,
									   final boolean caseSensitivity) {
		if (caseSensitivity) {
			return (fix instanceof HasName) &&
						   ((HasName) fix).getName().contains(pattern);
		} else {
			return (fix instanceof HasName)
						   && ((HasName) fix).getName().toLowerCase()
									  .contains(pattern);
		}
	}

	/**
	 * Search for the current pattern. If the pattern is found (as the ID of a
	 * fixture, or
	 * the name of a hasName, or the kind of a hasKind), select the tile containing the
	 * thing found. If the pattern is the empty string, don't search.
	 */
	public void search() {
		final String pattern;
		final boolean caseSensitivity = caseSensitive.isSelected();
		if (caseSensitivity) {
			pattern = search.getText();
		} else {
			pattern = search.getText().toLowerCase();
		}
		if (pattern.isEmpty()) {
			return;
		}
		int idNum = Integer.MIN_VALUE;
		if (IsNumeric.isNumeric(pattern)) {
			try {
				idNum = NUM_PARSER.parse(pattern).intValue();
			} catch (final ParseException e) {
				LOGGER.log(Level.SEVERE, "Pattern we detected as numeric wasn't", e);
			}
		}
		final int finalID = idNum;
		final Optional<Point> result =
				new PointIterator(map.getMapDimensions(), map.getSelectedPoint(),
										 !backwards.isSelected(),
										 !vertically.isSelected()).stream()
						.filter(point -> map.getMap().streamAllFixtures(point).anyMatch(
								fix -> matches(pattern, finalID, fix, caseSensitivity)))
						.findFirst();
		if (result.isPresent()) {
			SYS_OUT.print("Found in point");
			SYS_OUT.println(result.get());
			map.setSelection(result.get());
		}
	}

	/**
	 * Whether the fixture matches the given pattern in any way we recognize.
	 * @param pattern         a pattern
	 * @param idNum           either MIN_INT, or (if pattern is numeric) its numeric
	 *                        equivalent
	 * @param fix             a fixture. May be null, in which case we return false.
	 * @param caseSensitivity whether to search case-sensitively
	 * @return whether the fixture matches the pattern or has id as its ID.
	 */
	private boolean matches(final String pattern, final int idNum,
							final IFixture fix, final boolean caseSensitivity) {
		if (matchesSimple(pattern, idNum, fix, caseSensitivity)) {
			return true;
		} else if (fix instanceof FixtureIterable) {
			return ((FixtureIterable<?>) fix).stream().anyMatch(
					member -> matches(pattern, idNum, member, caseSensitivity));
		} else {
			return false;
		}
	}

	/**
	 * Whether the fixture matches the pattern in any of our simple ways.
	 * @param pattern         a pattern
	 * @param idNum           either MIN_INT, or (if pattern is numeric) its numeric
	 *                        equivalent
	 * @param fix             a fixture.
	 * @param caseSensitivity whether to search case-sensitively
	 * @return whether the fixture has id as its ID or matches the pattern in any of the
	 * simple ways; if this fails the caller will go on to the recursive test.
	 */
	private boolean matchesSimple(final String pattern, final int idNum,
								  final IFixture fix, final boolean caseSensitivity) {
		return !pattern.isEmpty() && (!(fix instanceof TileFixture) ||
											  ffl.shouldDisplay((TileFixture) fix)) &&
					   ((fix.getID() == idNum) ||
								matchesName(pattern, fix, caseSensitivity) ||
								matchesKind(pattern, fix, caseSensitivity) ||
								matchesOwner(pattern, idNum, fix, caseSensitivity));
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Report the last-searched-for pattern.
	 * @return a diagnostic String
	 */
	@Override
	public String toString() {
		return "FindDialog: last searched for " + search.getText();
	}

	/**
	 * A class to make sure the filter knows about all kinds of fixtures.
	 *
	 * @author Jonathan Lovelace
	 */
	private static final class FilterPopulator implements Runnable {
		/**
		 * The filter to populate.
		 */
		private final ZOrderFilter filter;
		/**
		 * The map to populate it from.
		 */
		private final IMapNG map;

		/**
		 * Constructor.
		 *
		 * @param ffm   the filter to populate
		 * @param model the map to populate it from
		 */
		protected FilterPopulator(final ZOrderFilter ffm, final IDriverModel model) {
			filter = ffm;
			map = model.getMap();
		}

		/**
		 * Run.
		 */
		@Override
		public void run() {
			for (final Point point : map.locations()) {
				if (map.getRivers(point).iterator().hasNext()) {
					//noinspection ObjectAllocationInLoop
					populate(new RiverFixture());
				}
				populate(map.streamAllFixtures(point));
			}
		}

		/**
		 * Populate the filter with the given fixture.
		 *
		 * @param fixture a fixture, or an iterable of fixtures, null
		 */
		private void populate(@Nullable final Object fixture) {
			if (fixture instanceof TileFixture) {
				filter.shouldDisplay((TileFixture) fixture);
			}
			if (fixture instanceof Iterable<?>) {
				for (final Object item : (Iterable<?>) fixture) {
					populate(item);
				}
			}
			if (fixture instanceof Stream<?>) {
				((Stream<?>) fixture).forEach(this::populate);
			}
		}

		/**
		 * A trivial toString().
		 * @return a String representation of the object
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
		@Override
		public String toString() {
			return "FilterPopulator";
		}
	}
}

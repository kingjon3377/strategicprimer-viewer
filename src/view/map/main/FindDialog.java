package view.map.main;

import static view.util.SystemOut.SYS_OUT;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import model.misc.IDriverModel;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

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
import model.map.fixtures.terrain.Mountain;
import model.viewer.IViewerModel;
import model.viewer.PointIterator;
import model.viewer.ZOrderFilter;
import util.IsNumeric;
import util.IteratorWrapper;
import util.NullCleaner;
import view.util.BorderedPanel;
import view.util.BoxPanel;
import view.util.ListenedButton;
import view.util.SplitWithWeights;

/**
 * A dialog to let the user find fixtures by ID, name, or "kind".
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
 *
 */
public final class FindDialog extends JDialog implements ActionListener {
	/**
	 * The proportion between the bulk of the dialog and the filter list.
	 */
	private static final double FILTER_PROPORTION = .6;
	/**
	 * The text field holding the search string.
	 */
	private final JTextField search = new JTextField("", 20);
	/**
	 * The map model to change the selection in.
	 */
	private final IViewerModel map;
	/**
	 * A label to display error messages.
	 */
	private final JLabel errorLabel = new JLabel(
			"This text should vanish from this label before the constructor ends.");
	/**
	 * The checkbox for searching backwards.
	 */
	private final JCheckBox backwards = new JCheckBox("Search backwards");
	/**
	 * The checkbox for searching vertically.
	 */
	private final JCheckBox vertically = new JCheckBox(
			"Search vertically then horizontally");
	/**
	 * The checkbox for making case-sensitive search.
	 */
	private final JCheckBox caseSensitive = new JCheckBox("Case-sensitive search");
	/**
	 * The filter, to let the user filter which fixtures are displayed.
	 */
	private final FixtureFilterList ffl;
	/**
	 * The frame that is this frame's parent.
	 */
	private final Frame parentFrame;

	/**
	 * Constructor.
	 *
	 * @param parent the parent to attach this dialog to
	 * @param model the map model to change the selection in
	 */
	public FindDialog(final Frame parent, final IViewerModel model) {
		super(parent);
		parentFrame = parent;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		search.addActionListener(this);
		search.setActionCommand("OK");
		errorLabel.setText("");
		errorLabel.setMinimumSize(new Dimension(200, 15));
		errorLabel.setAlignmentX(CENTER_ALIGNMENT);
		errorLabel.setAlignmentY(TOP_ALIGNMENT);

		final BoxPanel contentPane = new BoxPanel(false);
		contentPane.add(search);
		contentPane.add(backwards);
		contentPane.add(vertically);
		contentPane.add(caseSensitive);

		final BoxPanel buttonPanel = new BoxPanel(true);
		buttonPanel.addGlue();
		buttonPanel.add(new ListenedButton("OK", this));
		buttonPanel.addGlue();
		buttonPanel.add(new ListenedButton("Cancel", this));
		buttonPanel.addGlue();
		contentPane.add(buttonPanel);
		ffl = new FixtureFilterList();
		SwingUtilities.invokeLater(new FilterPopulator(ffl, model));
		setContentPane(new SplitWithWeights(JSplitPane.HORIZONTAL_SPLIT,
				FILTER_PROPORTION, FILTER_PROPORTION, contentPane,
				new BorderedPanel(new JScrollPane(ffl,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
						new JLabel("Find only ..."), null, null, null)));
		map = model;
		pack();
	}

	/**
	 *
	 * @param event the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent event) {
		if (event != null) {
			if ("OK".equals(event.getActionCommand())) {
				search();
			}
			setVisible(false);
			parentFrame.requestFocus();
			dispose();
		}
	}
	/**
	 * A parser to convert from strings to integers.
	 */
	private static final NumberFormat NUM_PARSER = NullCleaner
			.assertNotNull(NumberFormat.getIntegerInstance());
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = NullCleaner.assertNotNull(Logger
			.getLogger(FindDialog.class.getName()));
	/**
	 * Search for the current pattern. If the pattern is found (as the ID of a
	 * fixture, or the name of a hasName, or the kind of a hasKind), select the
	 * tile containing the thing found. If the pattern is the empty string,
	 * don't search.
	 */
	public void search() {
		final String pattern;
		final boolean csen = caseSensitive.isSelected();
		if (csen) {
			pattern = search.getText();
		} else {
			pattern = search.getText().toLowerCase();
		}
		if (pattern.isEmpty()) {
			return; // NOPMD
		}
		int idNum = Integer.MIN_VALUE;
		if (IsNumeric.isNumeric(pattern)) {
			try {
				idNum = NUM_PARSER.parse(pattern).intValue();
			} catch (final ParseException e) {
				LOGGER.log(Level.SEVERE, "Pattern we detected as numeric wasn't", e);
			}
		}
		final Iterable<Point> iter = new IteratorWrapper<>(new PointIterator(
				map.getMapDimensions(), map.getSelectedPoint(),
				!backwards.isSelected(), !vertically.isSelected()));
		for (final Point point : iter) {
			final TileFixture ground = map.getMap().getGround(point);
			final TileFixture forest = map.getMap().getForest(point);
			if ((ground != null && matches(pattern, idNum, ground, csen))
					|| (forest != null && matches(pattern, idNum, forest, csen))) {
				SYS_OUT.print("Found in point");
				SYS_OUT.println(point);
				map.setSelection(point);
				return;
			}
			for (final TileFixture fix : map.getMap().getOtherFixtures(point)) {
				if (matches(pattern, idNum, fix, csen)) {
					SYS_OUT.print("Found in point");
					SYS_OUT.println(point);
					map.setSelection(point);
					return;
				}
			}
		}
	}

	/**
	 * @param pattern a pattern
	 * @param idNum either MIN_INT, or (if pattern is numeric) its numeric
	 *        equivalent
	 * @param fix a fixture. May be null, in which case we return false.
	 * @param csen whether to search case-sensitively
	 * @return whether the fixture matches the pattern or has id as its ID.
	 */
	private boolean matches(final String pattern, final int idNum,
			final IFixture fix, final boolean csen) {
		if (matchesSimple(pattern, idNum, fix, csen)) {
			return true; // NOPMD
		} else if (fix instanceof FixtureIterable) {
			return StreamSupport.stream(((FixtureIterable<@NonNull ?>) fix).spliterator(), false)
					       .anyMatch((IFixture member) -> matches(pattern, idNum, member, csen));
		}
		return false;
	}

	/**
	 * @param pattern a pattern
	 * @param idNum either MIN_INT, or (if pattern is numeric) its numeric
	 *        equivalent
	 * @param fix a fixture.
	 * @param csen whether to search case-sensitively
	 * @return whether the fixture has id as its ID or matches the pattern in
	 *         any of the simple ways; if this fails the caller will go on to
	 *         the recursive test.
	 */
	private boolean matchesSimple(final String pattern, final int idNum,
			final IFixture fix, final boolean csen) {
		return !pattern.isEmpty()
				&& (!(fix instanceof TileFixture) || ffl
						.shouldDisplay((TileFixture) fix))
				&& (fix.getID() == idNum || matchesName(pattern, fix, csen)
						|| matchesKind(pattern, fix, csen) || matchesOwner(pattern,
							idNum, fix, csen));
	}

	/**
	 * @param pattern a pattern
	 * @param idNum the ID number that is the pattern if the pattern is numeric
	 * @param fix a fixture
	 * @param csen whether to search case-sensitively
	 * @return whether the fixture has an owner that matches the pattern.
	 */
	private static boolean matchesOwner(final String pattern, final int idNum,
			final IFixture fix, final boolean csen) {
		if (fix instanceof HasOwner) {
			final Player owner = ((HasOwner) fix).getOwner();
			final String ownerName;
			if (csen) {
				ownerName = owner.getName();
			} else {
				ownerName = owner.getName().toLowerCase();
			}
			return owner.getPlayerId() == idNum || ownerName.contains(pattern)
					|| matchesOwnerSpecials(pattern, owner);
		} else {
			return false;
		}
	}

	/**
	 * @param player the player owning a fixture
	 * @param pattern
	 *            a pattern
	 * @return whether the pattern is "me" and the fixture is owned by the
	 *         current player, or if the pattern is "none" and the fixture is
	 *         independent.
	 */
	private static boolean matchesOwnerSpecials(final String pattern,
			final Player player) {
		return "me".equalsIgnoreCase(pattern.trim())
				&& player.isCurrent()
				|| "none".equalsIgnoreCase(pattern.trim())
				&& player.isIndependent();
	}
	/**
	 * @param pattern a pattern
	 * @param fix a fixture
	 * @param csen whether to search case-sensitively
	 * @return whether the fixture has a 'kind' that matches the pattern
	 */
	private static boolean matchesKind(final String pattern,
			final IFixture fix, final boolean csen) {
		if (csen) {
			return fix instanceof HasKind && ((HasKind) fix).getKind()
					.contains(pattern);
		} else {
			return fix instanceof HasKind
					&& ((HasKind) fix).getKind().toLowerCase()
							.contains(pattern);
		}
	}

	/**
	 * @param pattern a patter
	 * @param fix a fixture
	 * @param csen whether to search case-sensitively
	 * @return whether the fixture has a name that matches the pattern
	 */
	private static boolean matchesName(final String pattern,
			final IFixture fix, final boolean csen) {
		if (csen) {
			return fix instanceof HasName
					&& ((HasName) fix).getName().contains(pattern);
		} else {
			return fix instanceof HasName
					&& ((HasName) fix).getName().toLowerCase()
							.contains(pattern);
		}
	}

	/**
	 * A class to make sure the filter knows about all kinds of fixtures.
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
		 * @param ffm the filter to populate
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
				populate(map.getGround(point));
				populate(map.getForest(point));
				if (map.isMountainous(point)) {
					populate(new Mountain());
				}
				if (map.getRivers(point).iterator().hasNext()) {
					populate(new RiverFixture());
				}
				populate(map.getOtherFixtures(point));
			}
		}
		/**
		 * Populate the filter with the given fixture.
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
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "FilterPopulator";
		}
	}
}

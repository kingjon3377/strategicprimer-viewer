package view.map.main;

import static view.util.SystemOut.SYS_OUT;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import model.map.FixtureIterable;
import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMap;
import model.map.ITile;
import model.map.Point;
import model.map.TileFixture;
import model.viewer.IViewerModel;
import model.viewer.PointIterator;
import model.viewer.ZOrderFilter;

import org.eclipse.jdt.annotation.Nullable;

import util.IsNumeric;
import util.IteratorWrapper;
import view.util.BorderedPanel;
import view.util.BoxPanel;
import view.util.ListenedButton;
import view.util.SplitWithWeights;

/**
 * A dialog to let the user find fixtures by ID, name, or "kind".
 *
 * @author Jonathan Lovelace
 *
 */
public class FindDialog extends JDialog implements ActionListener {
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
			"This text should vanish from the error-message label before the constructor ends.");
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
	 * The filter, to let the user filter which fixtures are displayed.
	 */
	private FixtureFilterList ffl;

	/**
	 * Constructor.
	 *
	 * @param parent the parent to attach this dialog to
	 * @param model the map model to change the selection in
	 */
	public FindDialog(final Frame parent, final IViewerModel model) {
		super(parent);
		search.addActionListener(this);
		search.setActionCommand("OK");
		errorLabel.setText("");
		errorLabel.setMinimumSize(new Dimension(200, 15));
		errorLabel.setAlignmentX(CENTER_ALIGNMENT);
		errorLabel.setAlignmentY(LEFT_ALIGNMENT);

		final BoxPanel contentPane = new BoxPanel(false);
		contentPane.add(search);
		contentPane.add(backwards);
		contentPane.add(vertically);

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
		}
	}

	/**
	 * Search for the current pattern. If the pattern is found (as the ID of a
	 * fixture, or the name of a hasName, or the kind of a hasKind), select the
	 * tile containing the thing found. If the pattern is the empty string,
	 * don't search.
	 */
	public void search() {
		final String pattern = search.getText();
		if (pattern.isEmpty()) {
			return; // NOPMD
		}
		int idNum = Integer.MIN_VALUE;
		if (IsNumeric.isNumeric(pattern)) {
			idNum = Integer.parseInt(pattern);
		}
		final Iterable<Point> iter = new IteratorWrapper<>(new PointIterator(
				map.getMapDimensions(), map.getSelectedPoint(),
				!backwards.isSelected(), !vertically.isSelected()));
		for (final Point point : iter) {
			if (point == null) {
				continue;
			}
			final ITile tile = map.getMap().getTile(point);
			for (final TileFixture fix : tile) {
				if (fix != null && matches(pattern, idNum, fix)) {
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
	 * @return whether the fixture matches the pattern or has id as its ID.
	 */
	private boolean matches(final String pattern, final int idNum,
			final IFixture fix) {
		if (matchesSimple(pattern, idNum, fix)) {
			return true; // NOPMD
		} else if (fix instanceof FixtureIterable<?>) {
			for (final IFixture member : (FixtureIterable<?>) fix) {
				if (member != null && matches(pattern, idNum, member)) {
					return true; // NOPMD
				}
			}
		}
		return false;
	}

	/**
	 * @param pattern a pattern
	 * @param idNum either MIN_INT, or (if pattern is numeric) its numeric
	 *        equivalent
	 * @param fix a fixture.
	 * @return whether the fixture has id as its ID or matches the pattern in
	 *         any of the simple ways; if this fails the caller will go on to
	 *         the recursive test.
	 */
	private boolean matchesSimple(final String pattern, final int idNum,
			final IFixture fix) {
		return !pattern.isEmpty()
				&& (!(fix instanceof TileFixture) || ffl
						.shouldDisplay((TileFixture) fix))
				&& (fix.getID() == idNum || matchesName(pattern, fix)
						|| matchesKind(pattern, fix) || matchesOwner(pattern,
							idNum, fix));
	}

	/**
	 * @param pattern a pattern
	 * @param idNum the ID number that is the pattern if the pattern is numeric
	 * @param fix a fixture
	 * @return whether the fixture has an owner that matches the pattern.
	 */
	private static boolean matchesOwner(final String pattern, final int idNum,
			final IFixture fix) {
		return fix instanceof HasOwner
				&& (((HasOwner) fix).getOwner().getName().contains(pattern)
						|| "me".equalsIgnoreCase(pattern.trim())
						&& ((HasOwner) fix).getOwner().isCurrent()
						|| "none".equalsIgnoreCase(pattern.trim())
						&& ((HasOwner) fix).getOwner().isIndependent() || ((HasOwner) fix)
						.getOwner().getPlayerId() == idNum);
	}

	/**
	 * @param pattern a pattern
	 * @param fix a fixture
	 * @return whether the fixture has a 'kind' that matches the pattern
	 */
	private static boolean matchesKind(final String pattern, final IFixture fix) {
		return fix instanceof HasKind && ((HasKind) fix).getKind()
				.contains(pattern);
	}

	/**
	 * @param pattern a patter
	 * @param fix a fixture
	 * @return whether the fixture has a name that matches the pattern
	 */
	private static boolean matchesName(final String pattern, final IFixture fix) {
		return fix instanceof HasName
				&& ((HasName) fix).getName().contains(pattern);
	}

	/**
	 * A class to make sure the filter knows about all kinds of fixtures.
	 * @author Jonathan Lovelace
	 */
	private static final class FilterPopulator implements Runnable {
		/**
		 * Constructor.
		 *
		 * @param ffm the filter to populate
		 * @param model the map to populate it from
		 */
		protected FilterPopulator(final ZOrderFilter ffm, final IViewerModel model) {
			filter = ffm;
			map = model.getMap();
		}

		/**
		 * The filter to populate.
		 */
		private final ZOrderFilter filter;
		/**
		 * The map to populate it from.
		 */
		private final IMap map;

		/**
		 * Run.
		 */
		@Override
		public void run() {
			for (final Point point : map.getTiles()) {
				if (point != null) {
					populate(map.getTiles().getTile(point));
				}
			}
		}

		/**
		 * Populate the filter.
		 *
		 * @param iter an iterable of fixtures.
		 */
		private void populate(final FixtureIterable<? extends IFixture> iter) {
			for (final IFixture item : iter) {
				if (item instanceof TileFixture) {
					filter.shouldDisplay((TileFixture) item);
				}
				if (item instanceof FixtureIterable<?>) {
					populate((FixtureIterable<?>) item);
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

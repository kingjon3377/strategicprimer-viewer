package drivers.map_viewer;

import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Arrays;

import legacy.map.fixtures.FixtureIterable;

import java.util.Objects;
import java.util.stream.StreamSupport;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.awt.Frame;

import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import lovelace.util.Platform;
import lovelace.util.ListenedButton;
import lovelace.util.BoxPanel;

import static lovelace.util.FunctionalSplitPane.horizontalSplit;
import static lovelace.util.BoxPanel.BoxAxis;

import lovelace.util.BorderedPanel;

import legacy.map.IFixture;
import common.map.HasName;
import legacy.map.HasKind;
import legacy.map.Player;
import legacy.map.HasOwner;
import legacy.map.TileFixture;
import legacy.map.PointIterable;
import legacy.map.Point;
import drivers.gui.common.SPDialog;

import java.util.function.Predicate;

/**
 * A dialog to let the user find fixtures by ID, name, or "kind".
 *
 * TODO: Add a "nearby" search (using {@link
 * exploration.common.SurroundingPointIterable} or a sort-by-distance
 * Comparator?)
 */
/* package */ final class FindDialog extends SPDialog {
	@Serial
	private static final long serialVersionUID = 1L;

	public FindDialog(final Frame parent, final IViewerModel model) {
		super(parent, "Find");
		this.model = model;
		this.parent = parent;
		searchField.addActionListener(ignored -> okListener());
		searchField.setActionCommand("OK");

		final BoxPanel buttonPanel = new BoxPanel(BoxAxis.LineAxis); // TODO: Use a better layout
		buttonPanel.addGlue();

		final JButton okButton = new ListenedButton("OK", this::okListener);

		final JButton cancelButton = new ListenedButton("Cancel", this::cancelListener);
		Platform.makeButtonsSegmented(okButton, cancelButton);
		buttonPanel.add(okButton);

		if (Platform.SYSTEM_IS_MAC) {
			searchField.putClientProperty("JTextField.variant", "search");
			searchField.putClientProperty("JTextField.Search.FindAction",
				(ActionListener) ignored -> okListener());
			searchField.putClientProperty("JTextField.Search.CancelAction",
				(ActionListener) ignored -> clearSearchField());
		} else {
			buttonPanel.addGlue();
		}

		buttonPanel.add(cancelButton);
		buttonPanel.addGlue();
		final JPanel contentPanel = BorderedPanel.verticalPanel(searchField,
			BorderedPanel.verticalPanel(backwards, vertically, caseSensitive), buttonPanel);

		SwingUtilities.invokeLater(this::populateAll);
		final JScrollPane scrollPane;
		if (Platform.SYSTEM_IS_MAC) { // TODO: combine with above
			scrollPane = new JScrollPane(filterList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		} else {
			scrollPane = new JScrollPane(filterList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}

		setContentPane(horizontalSplit(contentPanel,
			BorderedPanel.verticalPanel(new JLabel("Find only ..."), scrollPane, null), 0.6));
		pack();
	}

	private final IViewerModel model;
	private final Frame parent;

	private final JTextField searchField = new JTextField("", 20);
	private final JCheckBox backwards = new JCheckBox("Search backwards");
	private final JCheckBox vertically = new JCheckBox("Search vertically then horizontally");
	private final JCheckBox caseSensitive = new JCheckBox("Case-sensitive search");
	private final FixtureFilterList filterList = new FixtureFilterList();

	// TODO: Extract a "case-insensitive search", since this currently
	// fails if case-insensitive search is selected but the pattern
	// contains an upper-case character. Though we lowercase the pattern
	// before calling anything in {@link search}.

	/**
	 * Whether the fixture has a name matching the given pattern.
	 */
	private static boolean matchesName(final String pattern, final IFixture fixture, final boolean caseSensitivity) {
		if (fixture instanceof final HasName named) {
			final String name = (caseSensitivity) ? named.getName() : named.getName().toLowerCase();
			return name.contains(pattern);
		} else {
			return false;
		}
	}

	/**
	 * Whether the fixture has a kind matching the given pattern.
	 */
	private static boolean matchesKind(final String pattern, final IFixture fixture, final boolean caseSensitivity) {
		if (fixture instanceof final HasKind hk) {
			final String kind = (caseSensitivity) ? hk.getKind() : hk.getKind().toLowerCase();
			return kind.contains(pattern);
		} else {
			return false;
		}
	}

	/**
	 * Whether the fixture has an owner matching the given pattern.
	 */
	private static boolean matchesOwner(final String pattern, final @Nullable Integer idNum, final IFixture fixture,
	                                    final boolean caseSensitivity) {
		if (fixture instanceof final HasOwner owned) {
			final Player owner = owned.owner();
			final String ownerName = (caseSensitivity) ? owner.getName() :
				owner.getName().toLowerCase();
			if (!Objects.isNull(idNum) && (owner.getPlayerId() == idNum || // FIXME: Parenthesization seems wrong here
				ownerName.contains(pattern))) {
				return true;
			} else if ("me".equalsIgnoreCase(pattern) && owner.isCurrent()) {
				return true;
			} else {
				return owner.isIndependent() && Arrays.asList("none", "independent").contains(pattern.toLowerCase());
			}
		} else {
			return false;
		}
	}

	/**
	 * Whether the fixture matches the pattern in any of our simple ways.
	 */
	private boolean matchesSimple(final String pattern, final @Nullable Integer idNum, final IFixture fixture,
	                              final boolean caseSensitivity) {
		if (pattern.isEmpty()) {
			return true;
		} else if (fixture instanceof final TileFixture tf && !filterList.shouldDisplay(tf)) {
			return false;
		} else if (!Objects.isNull(idNum) && idNum == fixture.getId()) {
			return true;
		} else {
			return matchesName(pattern, fixture, caseSensitivity) ||
				matchesKind(pattern, fixture, caseSensitivity) ||
				matchesOwner(pattern, idNum, fixture, caseSensitivity);
		}
	}

	/**
	 * Whether the given fixture matches the given pattern in any way we recognize.
	 */
	private Predicate<IFixture> matches(final String pattern, final @Nullable Integer idNum,
	                                    final boolean caseSensitivity) {
		return fixture -> {
			if (matchesSimple(pattern, idNum, fixture, caseSensitivity)) {
				return true;
			} else if (fixture instanceof FixtureIterable) {
				return ((FixtureIterable<?>) fixture).stream().anyMatch(matches(pattern, idNum, caseSensitivity));
			} else {
				return false;
			}
		};
	}

	private Predicate<Point> matchesPoint(final String pattern, final @Nullable Integer id,
	                                      final boolean caseSensitivity) {
		return point -> {
			if ((caseSensitivity ? "bookmark".equals(pattern) : "bookmark".equalsIgnoreCase(pattern)) &&
				model.getMap().getBookmarks().contains(point)) {
				return true;
			} else {
				return model.getMap().getFixtures(point).stream()
					.anyMatch(matches(pattern, id, caseSensitivity));
			}
		};
	}

	/**
	 * Search for the current pattern. If the pattern is found (as the ID
	 * of a fixture, or the name of a {@link HasName}, or the kind of a
	 * {@link HasKind}), select the tile containing the thing found. If the
	 * pattern is the empty string, don't search.
	 */
	public void search() {
		final String pattern;
		final boolean caseSensitivity = caseSensitive.isSelected();
		if (caseSensitivity) {
			pattern = searchField.getText().strip();
		} else {
			pattern = searchField.getText().strip().toLowerCase();
		}
		if (pattern.isEmpty()) {
			return;
		}
		Integer idNum = null;
		try {
			idNum = Integer.parseInt(pattern);
		} catch (final NumberFormatException ignored) {
			// ignore non-numeric patterns
		}
		final Point result = StreamSupport.stream(new PointIterable(model.getMapDimensions(),
				!backwards.isSelected(), !vertically.isSelected(),
				model.getSelection()).spliterator(), false)
			.filter(matchesPoint(pattern, idNum, caseSensitivity)).findFirst().orElse(null);
		if (!Objects.isNull(result)) {
			LovelaceLogger.debug("Found in point %s", result);
			model.setSelection(result);
		}
	}

	private void okListener() {
		search();
		setVisible(false);
		parent.requestFocus();
		dispose();
	}

	private void cancelListener() {
		setVisible(false);
		parent.requestFocus();
		dispose();
	}

	private void clearSearchField() {
		searchField.setText("");
	}

	private void populate(final Object fixture) {
		if (fixture instanceof final TileFixture tf) {
			filterList.shouldDisplay(tf);
		} else if (fixture instanceof Iterable) {
			((Iterable<?>) fixture).forEach(this::populate);
		}
	}

	private void populateAll() {
		model.getMap().streamAllFixtures().forEach(this::populate);
	}

}

package drivers.map_viewer;

import java.io.Serial;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;

import drivers.common.VersionChangeListener;
import drivers.common.SelectionChangeListener;
import legacy.idreg.IDFactoryFiller;
import legacy.idreg.IDRegistrar;
import legacy.map.Point;
import legacy.map.TileType;
import legacy.map.River;
import legacy.map.TileFixture;
import legacy.map.fixtures.mobile.IUnit;

import java.util.Map;

import legacy.map.fixtures.terrain.Hill;

/**
 * A popup menu to let the user change a tile's terrain type, or add a unit.
 */
/* package */ final class TerrainChangingMenu extends JPopupMenu
		implements VersionChangeListener, SelectionChangeListener {
	@Serial
	private static final long serialVersionUID = 1L;
	private final IViewerModel model;
	private final IDRegistrar idf;
	private final NewUnitDialog nuDialog;
	private final SelectionChangeSupport scs = new SelectionChangeSupport();
	private final JMenuItem newUnitItem = new JMenuItem("Add New Unit ...");
	private Point point = Point.INVALID_POINT;
	private final JCheckBoxMenuItem mountainItem = new JCheckBoxMenuItem("Mountainous");
	private final JCheckBoxMenuItem hillItem = new JCheckBoxMenuItem("Hill(s)");
	private final JMenuItem newForestItem = new JMenuItem("Add New Forest ...");
	private final NewForestDialog nfDialog;
	private final JMenuItem textNoteItem = new JMenuItem("Add Text Note ...");
	private final JCheckBoxMenuItem bookmarkItem = new JCheckBoxMenuItem("Bookmarked");
	private final Map<River, JCheckBoxMenuItem> riverItems = new EnumMap<>(River.class);
	private static final List<River> RIVER_CHOICES = List.of(River.values());

	public TerrainChangingMenu(final int mapVersion, final IViewerModel model) {
		// TODO: Pass checkbox models into methods, not the items themselves?
		this.model = model;
		idf = IDFactoryFiller.createIDFactory(model.getMap());
		nuDialog = new NewUnitDialog(model.getMap().getCurrentPlayer(), idf);
		// TODO: convert to lambda?
		nuDialog.addNewUnitListener(unit -> {
			model.addFixture(point, unit);
			// FIXME: Extract a method for the 'set modified flag, fire changes, reset interaction' procedure
			model.setSelection(point);
			scs.fireChanges(null, point);
			model.setInteraction(null);
		});

		mountainItem.setMnemonic(KeyEvent.VK_M);
		mountainItem.addActionListener(ignored -> toggleMountains());
		hillItem.setMnemonic(KeyEvent.VK_H);
		hillItem.addActionListener(ignored -> toggleHill());

		newForestItem.setMnemonic(KeyEvent.VK_F);
		nfDialog = new NewForestDialog(idf);
		nfDialog.addNewFixtureListener(this::newFixture);

		textNoteItem.setMnemonic(KeyEvent.VK_X);
		final TextNoteDialog tnDialog = new TextNoteDialog(this::currentTurn);
		tnDialog.addNewFixtureListener(this::newFixture);

		bookmarkItem.setMnemonic(KeyEvent.VK_B);
		bookmarkItem.addActionListener(ignored -> toggleBookmarked());

		for (final River direction : RIVER_CHOICES) {
			final String desc;
			final int mnemonic;
			// TODO: Can we get this effect w/out going one-by-one here? e.g. for non-lakes desc = direction + " river"
			switch (direction) {
				case Lake -> {
					desc = "lake";
					mnemonic = KeyEvent.VK_K;
				}
				case North -> {
					desc = "north river";
					mnemonic = KeyEvent.VK_N;
				}
				case South -> {
					desc = "south river";
					mnemonic = KeyEvent.VK_S;
				}
				case East -> {
					desc = "east river";
					mnemonic = KeyEvent.VK_E;
				}
				case West -> {
					desc = "west river";
					mnemonic = KeyEvent.VK_W;
				}
				default -> throw new IllegalStateException("Exhaustive switch wasn't");
			}
			final JCheckBoxMenuItem item = new JCheckBoxMenuItem(desc);
			item.setMnemonic(mnemonic);
			final Runnable runnable = toggleRiver(direction, item);
			item.addActionListener(ignored -> runnable.run());
			riverItems.put(direction, item);
		}

		// TODO: Make some way to manipulate roads?

		updateForVersion(mapVersion);
		newUnitItem.addActionListener(ignored -> nuDialog.showWindow());
		newForestItem.addActionListener(ignored -> nfDialog.showWindow());
		textNoteItem.addActionListener(ignored -> tnDialog.showWindow());
		tnDialog.dispose();
		nfDialog.dispose();
		nuDialog.dispose();
	}

	private void toggleMountains() {
		final Point localPoint = point;
		final TileType terrain = model.getMap().getBaseTerrain(localPoint);
		if (localPoint.isValid() && !Objects.isNull(terrain) && TileType.Ocean != terrain) {
			final boolean newValue = !model.getMap().isMountainous(localPoint); // TODO: syntax sugar
			model.setMountainous(localPoint, newValue);
			mountainItem.getModel().setSelected(newValue);
			scs.fireChanges(null, localPoint);
		} else if (localPoint.isValid() && Objects.isNull(terrain)) {
			final boolean newValue = !model.getMap().isMountainous(localPoint);
			model.setMountainous(localPoint, newValue);
			mountainItem.getModel().setSelected(newValue);
			scs.fireChanges(null, localPoint);
		}
		model.setInteraction(null);
	}

	private void toggleHill() {
		final Point localPoint = point;
		final TileType terrain = model.getMap().getBaseTerrain(localPoint);
		if (localPoint.isValid() && !Objects.isNull(terrain) && TileType.Ocean != terrain) {
			if (model.getMap().getFixtures(localPoint).stream()
					.noneMatch(Hill.class::isInstance)) {
				model.addFixture(localPoint, new Hill(idf.createID()));
			} else {
				model.removeMatchingFixtures(localPoint, Hill.class::isInstance);
			}
			hillItem.getModel().setSelected(model.getMap().getFixtures(localPoint)
					.stream().anyMatch(Hill.class::isInstance));
			scs.fireChanges(null, localPoint);
		} else if (localPoint.isValid() && Objects.isNull(terrain)) {
			if (model.getMap().getFixtures(localPoint).stream()
					.noneMatch(Hill.class::isInstance)) {
				model.addFixture(localPoint, new Hill(idf.createID()));
			} else {
				model.removeMatchingFixtures(localPoint, Hill.class::isInstance);
			}
			hillItem.getModel().setSelected(model.getMap().getFixtures(localPoint)
					.stream().anyMatch(Hill.class::isInstance));
			scs.fireChanges(null, localPoint);
		}
		model.setInteraction(null);
	}

	private void newFixture(final TileFixture fixture) {
		model.addFixture(point, fixture);
		model.setSelection(point); // TODO: We probably don't always want to do this ...
		scs.fireChanges(null, point);
		model.setInteraction(null);
	}

	private int currentTurn() { // TODO: inline into lambda in caller?
		return model.getMap().getCurrentTurn();
	}

	private void toggleBookmarked() {
		final Point localPoint = point;
		if (model.getMap().getBookmarks().contains(localPoint)) { // TODO: make a 'bookmarks' accessor in model?
			model.removeBookmark(localPoint);
			bookmarkItem.getModel().setSelected(false);
		} else {
			model.addBookmark(localPoint);
			bookmarkItem.getModel().setSelected(true);
		}
		scs.fireChanges(null, localPoint);
		model.setInteraction(null);
	}

	private Runnable toggleRiver(final River river, final JCheckBoxMenuItem item) {
		return () -> {
			final Point localPoint = point;
			final TileType terrain = model.getMap().getBaseTerrain(localPoint);
			if (localPoint.isValid() && !Objects.isNull(terrain) && TileType.Ocean != terrain) {
				if (model.getMap().getRivers(localPoint).contains(river)) {
					model.removeRiver(localPoint, river);
					item.getModel().setSelected(false);
				} else {
					model.addRiver(localPoint, river);
					item.getModel().setSelected(true);
				}
				scs.fireChanges(null, localPoint);
				model.setInteraction(null);
			} else if (localPoint.isValid() && Objects.isNull(terrain)) {
				if (model.getMap().getRivers(localPoint).contains(river)) {
					model.removeRiver(localPoint, river);
					item.getModel().setSelected(false);
				} else {
					model.addRiver(localPoint, river);
					item.getModel().setSelected(true);
				}
				scs.fireChanges(null, localPoint);
				model.setInteraction(null);
			}
		};
	}

	private void removeTerrain(final ActionEvent event) { // TODO: Why take the event here?
		model.setBaseTerrain(point, null);
		scs.fireChanges(null, point);
		model.setInteraction(null);
	}

	private void updateForVersion(final int version) {
		removeAll();
		add(bookmarkItem);
		add(textNoteItem);
		addSeparator();
		final JMenuItem removalItem = new JMenuItem("Remove terrain");
		removalItem.setMnemonic(KeyEvent.VK_V);
		add(removalItem);
		removalItem.addActionListener(this::removeTerrain);
		for (final TileType type : TileType.getValuesForVersion(version)) {
			final String desc;
			final int mnemonic;
			switch (type) {
				case Tundra -> {
					desc = "tundra";
					mnemonic = KeyEvent.VK_T;
				}
				case Desert -> {
					desc = "desert";
					mnemonic = KeyEvent.VK_D;
				}
				case Ocean -> {
					desc = "ocean";
					mnemonic = KeyEvent.VK_O;
				}
				case Plains -> {
					desc = "plains";
					mnemonic = KeyEvent.VK_L;
				}
				case Jungle -> {
					desc = "jungle";
					mnemonic = KeyEvent.VK_J;
				}
				case Steppe -> {
					desc = "steppe";
					mnemonic = KeyEvent.VK_P;
				}
				case Swamp -> {
					desc = "swamp";
					mnemonic = KeyEvent.VK_A;
				}
				default ->
					// desc = type.toString();
					// mnemonic = null;
						throw new IllegalStateException("Exhaustive switch wasn't");
			}
			final JMenuItem item = new JMenuItem(desc);
			item.setMnemonic(mnemonic);
			add(item);
			item.addActionListener(event -> {
				model.setBaseTerrain(point, type);
				scs.fireChanges(null, point);
				model.setInteraction(null);
			});
		}
		addSeparator();
		add(newUnitItem);
		add(mountainItem);
		add(hillItem);
		add(newForestItem);
		riverItems.values().forEach(this::add);
	}

	@Override
	public void changeVersion(final int old, final int newVersion) {
		updateForVersion(newVersion);
	}

	@Override
	public void selectedPointChanged(final @Nullable Point old, final Point newPoint) {
	}

	@Override
	public void cursorPointChanged(final @Nullable Point old, final Point newCursor) {
	}

	@Override
	public void interactionPointChanged() {
		// We default to the selected point if the model has no
		// interaction point, in case the menu gets shown before the
		// interaction point gets set somehow.
		final Point localPoint = Optional.ofNullable(model.getInteraction()).orElseGet(model::getSelection);
		point = localPoint;
		final TileType terrain = model.getMap().getBaseTerrain(point);
		newUnitItem.setEnabled(point.isValid() && !Objects.isNull(terrain));
		if (point.isValid() && !Objects.isNull(terrain) && TileType.Ocean != terrain) {
			mountainItem.getModel().setSelected(model.getMap().isMountainous(point));
			mountainItem.setEnabled(true);
			hillItem.getModel().setSelected(model.getMap().getFixtures(point).stream()
					.anyMatch(Hill.class::isInstance));
			hillItem.setEnabled(true);
			newForestItem.setEnabled(true);
			final Collection<River> rivers = model.getMap().getRivers(point);
			for (final Map.Entry<River, JCheckBoxMenuItem> entry : riverItems.entrySet()) {
				entry.getValue().setEnabled(true);
				entry.getValue().getModel().setSelected(rivers.contains(entry.getKey()));
			}
		} else if (point.isValid() && Objects.isNull(terrain)) {
			mountainItem.getModel().setSelected(model.getMap().isMountainous(point));
			mountainItem.setEnabled(true);
			hillItem.getModel().setSelected(model.getMap().getFixtures(point).stream()
					.anyMatch(Hill.class::isInstance));
			hillItem.setEnabled(true);
			newForestItem.setEnabled(true);
			final Collection<River> rivers = model.getMap().getRivers(point);
			for (final Map.Entry<River, JCheckBoxMenuItem> entry : riverItems.entrySet()) {
				entry.getValue().setEnabled(true);
				entry.getValue().getModel().setSelected(rivers.contains(entry.getKey()));
			}
		} else {
			mountainItem.getModel().setSelected(false);
			mountainItem.setEnabled(false);
			hillItem.getModel().setSelected(false);
			hillItem.setEnabled(false);
			newForestItem.setEnabled(false);
			for (final Map.Entry<River, JCheckBoxMenuItem> entry : riverItems.entrySet()) {
				entry.getValue().getModel().setSelected(false);
				entry.getValue().setEnabled(false);
			}
		}
		bookmarkItem.getModel().setSelected(model.getMap().getBookmarks().contains(localPoint));
	}

	@Override
	public void selectedUnitChanged(final @Nullable IUnit oldSelection, final @Nullable IUnit newSelection) {
	}
}


package drivers.map_viewer;

import java.util.Collection;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;

import drivers.common.VersionChangeListener;
import drivers.common.SelectionChangeListener;
import drivers.common.NewFixtureListener;
import worker.common.NewUnitListener;
import common.idreg.IDFactoryFiller;
import common.idreg.IDRegistrar;
import common.map.Point;
import common.map.TileType;
import common.map.River;
import common.map.TileFixture;
import common.map.fixtures.mobile.IUnit;

import java.util.Map;
import java.util.HashMap;
import common.map.fixtures.terrain.Hill;
import drivers.gui.common.SPDialog;

/**
 * A popup menu to let the user change a tile's terrain type, or add a unit.
 */
/* package */ class TerrainChangingMenu extends JPopupMenu
		implements VersionChangeListener, SelectionChangeListener {
	private int mapVersion;
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
	private final Map<River, JCheckBoxMenuItem> riverItems = new HashMap<>();

	public TerrainChangingMenu(int mapVersion, IViewerModel model) {
		// TODO: Pass checkbox models into methods, not the items themselves?
		this.mapVersion = mapVersion;
		this.model = model;
		idf = new IDFactoryFiller().createIDFactory(model.getMap());
		nuDialog = new NewUnitDialog(model.getMap().getCurrentPlayer(), idf);
		nuDialog.addNewUnitListener(new NewUnitListener() { // TODO: convert to lambda?
				@Override
				public void addNewUnit(IUnit unit) {
					model.addFixture(point, unit);
					model.setSelection(point); // FIXME: Extract a method for the 'set modified flag, fire changes, reset interaction' procedure
					scs.fireChanges(null, point);
					model.setInteraction(null);
				}
			});

		mountainItem.setMnemonic(KeyEvent.VK_M);
		mountainItem.addActionListener(ignored -> toggleMountains());
		hillItem.setMnemonic(KeyEvent.VK_H);
		hillItem.addActionListener(ignored -> toggleHill());

		newForestItem.setMnemonic(KeyEvent.VK_F);
		nfDialog = new NewForestDialog(idf);
		nfDialog.addNewFixtureListener(this::newFixture);

		textNoteItem.setMnemonic(KeyEvent.VK_X);
		TextNoteDialog tnDialog = new TextNoteDialog(this::currentTurn);
		tnDialog.addNewFixtureListener(this::newFixture);

		bookmarkItem.setMnemonic(KeyEvent.VK_B);
		bookmarkItem.addActionListener(ignored -> toggleBookmarked());

		for (River direction : River.values()) {
			String desc;
			int mnemonic;
			switch (direction) {
			case Lake:
				desc = "lake";
				mnemonic = KeyEvent.VK_K;
				break;
			case North:
				desc = "north river";
				mnemonic = KeyEvent.VK_N;
				break;
			case South:
				desc = "south river";
				mnemonic = KeyEvent.VK_S;
				break;
			case East:
				desc = "east river";
				mnemonic = KeyEvent.VK_E;
				break;
			case West:
				desc = "west river";
				mnemonic = KeyEvent.VK_W;
				break;
			default:
				throw new IllegalStateException("Exhaustive switch wasn't");
			}
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(desc);
			item.setMnemonic(mnemonic);
			Runnable runnable = toggleRiver(direction, item);
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
		Point localPoint = point;
		TileType terrain = model.getMap().getBaseTerrain(localPoint);
		if (localPoint.isValid() && terrain != null && !TileType.Ocean.equals(terrain)) {
			boolean newValue = !model.getMap().isMountainous(localPoint); // TODO: syntax sugar
			model.setMountainous(localPoint, newValue);
			mountainItem.getModel().setSelected(newValue);
			scs.fireChanges(null, localPoint);
		}
		model.setInteraction(null);
	}

	private void toggleHill() {
		Point localPoint = point;
		TileType terrain = model.getMap().getBaseTerrain(localPoint);
		if (localPoint.isValid() && terrain != null && !TileType.Ocean.equals(terrain)) {
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

	private void newFixture(TileFixture fixture) {
		model.addFixture(point, fixture);
		model.setSelection(point); // TODO: We probably don't always want to do this ...
		scs.fireChanges(null, point);
		model.setInteraction(null);
	}

	private int currentTurn() { // TODO: inline into lambda in caller?
		return model.getMap().getCurrentTurn();
	}

	private void toggleBookmarked() {
		Point localPoint = point;
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

	private Runnable toggleRiver(River river, JCheckBoxMenuItem item) {
		return () -> {
			Point localPoint = point;
			TileType terrain = model.getMap().getBaseTerrain(localPoint);
			if (localPoint.isValid() && terrain != null && !TileType.Ocean.equals(terrain)) {
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

	private void removeTerrain(ActionEvent event) { // TODO: Why take the event here?
		model.setBaseTerrain(point, null);
		scs.fireChanges(null, point);
		model.setInteraction(null);
	}

	private void updateForVersion(int version) {
		removeAll();
		add(bookmarkItem);
		add(textNoteItem);
		addSeparator();
		JMenuItem removalItem = new JMenuItem("Remove terrain");
		removalItem.setMnemonic(KeyEvent.VK_V);
		add(removalItem);
		removalItem.addActionListener(this::removeTerrain);
		for (TileType type : TileType.getValuesForVersion(version)) {
			String desc;
			Integer mnemonic;
			switch (type) {
			case Tundra:
				desc = "tundra";
				mnemonic = KeyEvent.VK_T;
				break;
			case Desert:
				desc = "desert";
				mnemonic = KeyEvent.VK_D;
				break;
			case Ocean:
				desc = "ocean";
				mnemonic = KeyEvent.VK_O;
				break;
			case Plains:
				desc = "plains";
				mnemonic = KeyEvent.VK_L;
				break;
			case Jungle:
				desc = "jungle";
				mnemonic = KeyEvent.VK_J;
				break;
			case Steppe:
				desc = "steppe";
				mnemonic = KeyEvent.VK_P;
				break;
			case Swamp:
				desc = "swamp";
				mnemonic = KeyEvent.VK_A;
				break;
			default:
				// desc = type.toString();
				// mnemonic = null;
				throw new IllegalStateException("Exhaustive switch wasn't");
			}
			JMenuItem item = new JMenuItem(desc);
			if (mnemonic != null) {
				item.setMnemonic(mnemonic);
			}
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
	public void changeVersion(int old, int newVersion) {
		updateForVersion(newVersion);
	}

	@Override
	public void selectedPointChanged(@Nullable Point old, Point newPoint) {}

	@Override
	public void cursorPointChanged(@Nullable Point old, Point newCursor) {}

	@Override
	public void interactionPointChanged() {
		// We default to the selected point if the model has no
		// interaction point, in case the menu gets shown before the
		// interaction point gets set somehow.
		Point localPoint = Optional.ofNullable(model.getInteraction()).orElseGet(model::getSelection);
		point = localPoint;
		TileType terrain = model.getMap().getBaseTerrain(point);
		if (point.isValid() && terrain != null) { // TODO: condense, like returning a boolean
			newUnitItem.setEnabled(true);
		} else {
			newUnitItem.setEnabled(false);
		}
		if (point.isValid() && terrain != null && !TileType.Ocean.equals(terrain)) {
			mountainItem.getModel().setSelected(model.getMap().isMountainous(point));
			mountainItem.setEnabled(true);
			hillItem.getModel().setSelected(model.getMap().getFixtures(point).stream()
				.anyMatch(Hill.class::isInstance));
			hillItem.setEnabled(true);
			newForestItem.setEnabled(true);
		} else {
			mountainItem.getModel().setSelected(false);
			mountainItem.setEnabled(false);
			hillItem.getModel().setSelected(false);
			hillItem.setEnabled(false);
			newForestItem.setEnabled(false);
		}
		if (point.isValid() && terrain != null && !TileType.Ocean.equals(terrain)) {
			// TODO: combine with earlier if(s)
			Collection<River> rivers = model.getMap().getRivers(point);
			for (Map.Entry<River, JCheckBoxMenuItem> entry : riverItems.entrySet()) {
				entry.getValue().setEnabled(true);
				entry.getValue().getModel().setSelected(rivers.contains(entry.getKey()));
			}
		} else {
			for (Map.Entry<River, JCheckBoxMenuItem> entry : riverItems.entrySet()) {
				entry.getValue().getModel().setSelected(false);
				entry.getValue().setEnabled(false);
			}
		}
		bookmarkItem.getModel().setSelected(model.getMap().getBookmarks().contains(localPoint));
	}

	@Override
	public void selectedUnitChanged(@Nullable IUnit oldSelection, @Nullable IUnit newSelection) {}
}

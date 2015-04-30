package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSource;
import model.listeners.SelectionChangeSupport;
import model.listeners.VersionChangeListener;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileType;
import model.misc.IDriverModel;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A popup menu to let the user change a tile's terrain type.
 *
 * @author Jonathan Lovelace
 */
public class TerrainChangingMenu extends JPopupMenu implements ActionListener,
		VersionChangeListener, SelectionChangeSource, SelectionChangeListener {
	/**
	 * The driver model.
	 */
	private final IDriverModel model;
	/**
	 * The point at which we might change terrain.
	 */
	private Point point = PointFactory.point(-1, -1);

	/**
	 * The helper to handle selection-change listeners for us.
	 */
	private final SelectionChangeSupport scs = new SelectionChangeSupport();

	/**
	 * Constructor.
	 *
	 * @param version the map version
	 * @param dmodel the driver model
	 */
	public TerrainChangingMenu(final int version, final IDriverModel dmodel) {
		model = dmodel;
		updateForVersion(version);
	}

	/**
	 * Update the menu for a new version.
	 *
	 * @param version the version
	 */
	private void updateForVersion(final int version) {
		for (final TileType type : TileType.valuesForVersion(version)) {
			final JMenuItem item = new JMenuItem(type.toString()); // NOPMD
			add(item);
			item.addActionListener(this);
		}
	}

	/**
	 * Handle Menu selections.
	 *
	 * @param event the menu-item-selected event we're handling.
	 */
	@Override
	public final void actionPerformed(@Nullable final ActionEvent event) {
		if (event != null) {
			final String command = event.getActionCommand();
			if (command != null) {
				model.getMap().setBaseTerrain(point, TileType.valueOf(command));
				scs.fireChanges(null, point);
			}
		}
	}

	/**
	 * @param old the previously selected version
	 * @param newVersion the newly selected version
	 */
	@Override
	public void changeVersion(final int old, final int newVersion) {
		updateForVersion(newVersion);
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public final void addSelectionChangeListener(
			final SelectionChangeListener list) {
		scs.addSelectionChangeListener(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public final void removeSelectionChangeListener(
			final SelectionChangeListener list) {
		scs.removeSelectionChangeListener(list);
	}

	/**
	 * @param old ignored
	 * @param newPoint ignored
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old,
			final Point newPoint) {
		point = newPoint;
	}
}

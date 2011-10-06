package view.map.main;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import model.viewer.Point;
import model.viewer.PointFactory;

/**
 * A class to keep track of which tile is selected.
 * 
 * @author Jonathan Lovelace
 */
// ESCA-JAVA0137:
public class TileSelectionListener extends SelectionListener {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(TileSelectionListener.class.getName());
	/**
	 * The menu to change a tile's type.
	 */
	private static final TerrainChangingMenu MENU = new TerrainChangingMenu();
	/**
	 * The main viewer ... the only object that can copy a tile from the main
	 * map to the alternate.
	 */
	private final MapPanel viewer;
	/**
	 * Constructor.
	 * 
	 * @param view
	 *            the main panel. Needed to copy a tile from the main map to a
	 *            sub-map.
	 */
	public TileSelectionListener(final MapPanel view) {
		super();
		viewer = view;
		view.getModel().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				getSupport().firePropertyChange(evt);
			}
			
		});
	}

	/**
	 * Handle mouse clicks.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		super.mouseClicked(event);
		if (selection() instanceof GUITile) {
			viewer.getModel().setSelection(((GUITile) selection()).getTile().getRow(), ((GUITile) selection()).getTile().getCol());
			if (event.getClickCount() == 2) {
				getSupport().firePropertyChange("encounter", "old", "new");
				viewer.getModel().copyTile(((GUITile) selection()).getTile());
			}
			selection().requestFocusInWindow();
			LOGGER.fine("Click");
		} else {
			viewer.getModel().clearSelection();
		}
		if (event.getSource() instanceof MapGUI) {
			((MapPanel) event.getSource()).repaint();
		}
	}
	/**
	 * Move the cursor up one.
	 */
	public void up() { // NOPMD
		if (selection() instanceof GUITile) {
			final Point point = PointFactory.point(
					((GUITile) selection()).getTile().getRow() - 1,
					((GUITile) selection()).getTile().getCol());
			final GUITile newTile = viewer.getTile(point); 
			setSelection(newTile);
			viewer.getModel().setSelection(point.row(), point.col());
		}
	}
	/**
	 * Move the cursor left one.
	 */
	public void left() {
		if (selection() instanceof GUITile) {
			final Point point = PointFactory.point(
					((GUITile) selection()).getTile().getRow(),
					((GUITile) selection()).getTile().getCol() - 1);
			final GUITile newTile = viewer.getTile(point); 
			setSelection(newTile);
			viewer.getModel().setSelection(point.row(), point.col());
		}
	}
	/**
	 * Move the cursor down one.
	 */
	public void down() { // NOPMD
		if (selection() instanceof GUITile) {
			final Point point = PointFactory.point(
					((GUITile) selection()).getTile().getRow() + 1,
					((GUITile) selection()).getTile().getCol());
			final GUITile newTile = viewer.getTile(point); 
			setSelection(newTile);
			viewer.getModel().setSelection(point.row(), point.col());
		}
	}
	/**
	 * Move the cursor right one.
	 */
	public void right() {
		if (selection() instanceof GUITile) {
			final Point point = PointFactory.point(
					((GUITile) selection()).getTile().getRow(),
					((GUITile) selection()).getTile().getCol() + 1);
			final GUITile newTile = viewer.getTile(point); 
			setSelection(newTile);
			viewer.getModel().setSelection(point.row(), point.col());
		}
	}
	/**
	 * Ignored.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		super.mousePressed(event);
		if (event.isPopupTrigger()) {
			MENU.setTile((GUITile) event.getComponent());
			MENU.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 * Ignored.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseReleased(final MouseEvent event) {
		super.mouseReleased(event);
		if (event.isPopupTrigger()) {
			MENU.setTile((GUITile) event.getComponent());
			MENU.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 * Clear the selection.
	 */
	@Override
	public void clearSelection() {
		super.clearSelection();
		viewer.getModel().clearSelection();
	}
}

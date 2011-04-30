package view.map;

import java.awt.event.MouseEvent;
import java.util.logging.Logger;

/**
 * A class to keep track of which tile is selected.
 * 
 * @author Jonathan Lovelace
 */
//ESCA-JAVA0137:
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
	 * The detail panel.
	 */
	private final DetailPanel detailPanel;
	/**
	 * Constructor.
	 * @param details the panel that'll show the details of the selected tile
	 */
	public TileSelectionListener(final DetailPanel details) {
		super();
		if (details == null) {
			throw new IllegalArgumentException("DetailPanel was null");
		}
		detailPanel = details;
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
		if (event.getComponent() instanceof GUITile) {
			detailPanel.setTile(((GUITile) (event.getComponent())).getTile());
			LOGGER.fine("Click");
		}
	}

	/**
	 * Ignored
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
	 * Ignored
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

}

package view.exploration;

import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JButton;

import model.map.Tile;
import model.map.TileType;
import view.map.main.TileDrawHelper;
import view.map.main.TileDrawHelperFactory;

/**
 * A button that represents a tile in two maps.
 * @author Jonathan Lovelace
 */
public class DualTileButton extends JButton {
	/**
	 * The main-map tile to paint.
	 */
	private Tile one = new Tile(TileType.NotVisible);
	/**
	 * The secondary-map tile to paint.
	 */
	private Tile two = new Tile(TileType.NotVisible);
	/**
	 * @param first the main-map tile to draw
	 * @param second the secondary-map tile to draw
	 */
	public void setTiles(final Tile first, final Tile second) {
		one = first;
		two = second;
	}
	/**
	 * How much margin to give.
	 */
	private static final int MARGIN = 2;
	/**
	 * Paint the component.
	 * @param pen the Graphics object to draw with.
	 */
	@Override
	public void paintComponent(final Graphics pen) {
		final TileDrawHelper helper = TileDrawHelperFactory.INSTANCE.factory(2, this);
		pen.setClip(new Polygon(
				new int[] { getWidth() - MARGIN, MARGIN, MARGIN }, new int[] {
						MARGIN, getHeight() - MARGIN, MARGIN }, 3));
		helper.drawTile(pen, one, getWidth(), getHeight());
		pen.setClip(new Polygon(new int[] { getWidth() - MARGIN,
				getWidth() - MARGIN, MARGIN }, new int[] { MARGIN,
				getHeight() - MARGIN, getHeight() - MARGIN }, 3));
		helper.drawTile(pen, two, getWidth(), getHeight());
	}
}
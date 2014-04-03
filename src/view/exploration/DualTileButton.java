package view.exploration;

import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JButton;

import model.map.ITile;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.viewer.ZOrderFilter;

import org.eclipse.jdt.annotation.Nullable;

import view.map.main.TileDrawHelper;
import view.map.main.TileDrawHelperFactory;

/**
 * A button that represents a tile in two maps.
 *
 * @author Jonathan Lovelace
 */
public class DualTileButton extends JButton {
	/**
	 * How much margin to give.
	 */
	private static final int MARGIN = 2;

	/**
	 * The main-map tile to paint.
	 */
	private ITile one = new Tile(TileType.NotVisible);
	/**
	 * The secondary-map tile to paint.
	 */
	private ITile two = new Tile(TileType.NotVisible);

	/**
	 * The ZOrderFilter instance to pass to the factory rather than null.
	 */
	private static final ZOrderFilter NULL_ZOF = new NullZOrderFilter();

	/**
	 * @param first the main-map tile to draw
	 * @param second the secondary-map tile to draw
	 */
	public void setTiles(final ITile first, final ITile second) {
		one = first;
		two = second;
	}

	/**
	 * A ZOrderFilter implementation that does nothing, to avoid passing null to
	 * the factory.
	 * @author Jonathan Lovelace
	 */
	private static final class NullZOrderFilter implements ZOrderFilter {
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "NullZOrderFilter";
		}
		/**
		 * Constructor. Only explicit to fix a synthetic-access warning.
		 */
		protected NullZOrderFilter() {
			// Do nothing
		}

		/**
		 * @param fix ignored
		 * @return true
		 */
		@Override
		public boolean shouldDisplay(final TileFixture fix) {
			return true;
		}
	}

	/**
	 * Paint the component.
	 *
	 * @param pen the Graphics object to draw with.
	 */
	@Override
	protected void paintComponent(@Nullable final Graphics pen) {
		if (pen == null) {
			throw new IllegalArgumentException("Graphics cannot be null");
		}
		super.paintComponent(pen);
		final TileDrawHelper helper = TileDrawHelperFactory.INSTANCE.factory(2,
				this, NULL_ZOF);
		pen.setClip(new Polygon(
				new int[] { getWidth() - MARGIN, MARGIN, MARGIN }, new int[] {
						MARGIN, getHeight() - MARGIN, MARGIN }, 3));
		helper.drawTileTranslated(pen, one, getWidth(), getHeight());
		pen.setClip(new Polygon(new int[] { getWidth() - MARGIN,
				getWidth() - MARGIN, MARGIN }, new int[] { MARGIN,
				getHeight() - MARGIN, getHeight() - MARGIN }, 3));
		helper.drawTileTranslated(pen, two, getWidth(), getHeight());
	}
}

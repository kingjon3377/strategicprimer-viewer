package view.exploration;

import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JButton;

import model.map.IMapNG;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
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
	 * The main map map.
	 */
	private final IMapNG mapOne;
	/**
	 * The subordinate map.
	 */
	private final IMapNG mapTwo;
	/**
	 * @param one the first map
	 * @param two the second map
	 */
	public DualTileButton(final IMapNG one, final IMapNG two) {
		mapOne = one;
		mapTwo = two;
	}
	/**
	 * The currently selected point.
	 */
	private Point point = PointFactory.point(-1, -1);
	/**
	 * The ZOrderFilter instance to pass to the factory rather than null.
	 */
	private static final ZOrderFilter NULL_ZOF = new NullZOrderFilter();

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
		helper.drawTileTranslated(pen, mapOne, point, getWidth(), getHeight());
		pen.setClip(new Polygon(new int[] { getWidth() - MARGIN,
				getWidth() - MARGIN, MARGIN }, new int[] { MARGIN,
				getHeight() - MARGIN, getHeight() - MARGIN }, 3));
		helper.drawTileTranslated(pen, mapTwo, point, getWidth(), getHeight());
	}
	/**
	 * Set the currently selected point.
	 * @param newPoint the newly selected point
	 */
	public void setPoint(final Point newPoint) {
		point = newPoint;
		repaint();
	}
}

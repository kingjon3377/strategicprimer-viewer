package drivers.exploration;

import java.awt.Graphics;
import java.awt.Polygon;

import java.util.stream.StreamSupport;
import javax.swing.JButton;

import drivers.map_viewer.TileDrawHelper;
import drivers.map_viewer.Ver2TileDrawHelper;
import drivers.map_viewer.Coordinate;

import common.map.Point;
import common.map.IMapNG;

import drivers.common.FixtureMatcher;

/**
 * A button (visually) representing a tile in two maps.
 */
/* package */ class DualTileButton extends JButton {
	private static final long serialVersionUID = 1L;
	private static final int MARGIN = 2;
	private Point localPoint = Point.INVALID_POINT;
	private final TileDrawHelper helper;
	private final IMapNG master;
	private final IMapNG subordinate;

	public DualTileButton(final IMapNG master, final IMapNG subordinate, final Iterable<FixtureMatcher> matchers) {
		this.master = master;
		this.subordinate = subordinate;
		helper = new Ver2TileDrawHelper(this, fix -> true,
				StreamSupport.stream(matchers.spliterator(), false).toArray(FixtureMatcher[]::new));
	}

	public Point getPoint() {
		return localPoint;
	}

	public void setPoint(final Point point) {
		localPoint = point;
		repaint();
	}

	@Override
	public void paintComponent(final Graphics pen) {
		super.paintComponent(pen);
		final Coordinate origin = new Coordinate(0, 0);
		final Coordinate dimensions = new Coordinate(getWidth(), getHeight());
		// TODO: cache the polygons until size changes
		pen.setClip(new Polygon(new int[] {getWidth() - MARGIN, MARGIN, MARGIN},
			new int[] { MARGIN, getHeight() - MARGIN, MARGIN }, 3));
		helper.drawTile(pen, master, localPoint, origin, dimensions);
		pen.setClip(new Polygon(new int[] { getWidth() - MARGIN, getWidth() - MARGIN, MARGIN },
			new int[] { MARGIN, getHeight() - MARGIN, getHeight() - MARGIN }, 3));
		helper.drawTile(pen, subordinate, localPoint, origin, dimensions);
		// FIXME: clear clip
	}
}

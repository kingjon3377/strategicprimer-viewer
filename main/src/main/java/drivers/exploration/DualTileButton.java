package drivers.exploration;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;

import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.Serial;
import java.util.Objects;
import java.util.stream.StreamSupport;
import javax.swing.JButton;

import drivers.map_viewer.TileDrawHelper;
import drivers.map_viewer.Ver2TileDrawHelper;
import drivers.map_viewer.Coordinate;

import legacy.map.Point;
import legacy.map.ILegacyMap;

import drivers.common.FixtureMatcher;
import org.jetbrains.annotations.Nullable;

/**
 * A button (visually) representing a tile in two maps.
 */
/* package */ final class DualTileButton extends JButton {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final int MARGIN = 2;
	private Point localPoint = Point.INVALID_POINT;
	private final TileDrawHelper helper;
	private final ILegacyMap master;
	private final ILegacyMap subordinate;

	/**
	 * Wrapper around {@code imageUpdate}, which is overloaded, so we can pass a method reference instead of "this"
	 */
	private boolean imageUpdateWrapper(final Image img, final int infoFlags, final int x, final int y, final int width, final int height) {
		return imageUpdate(img, infoFlags, x, y, width, height);
	}

	public DualTileButton(final ILegacyMap master, final ILegacyMap subordinate,
						  final Iterable<FixtureMatcher> matchers) {
		this.master = master;
		this.subordinate = subordinate;
		helper = new Ver2TileDrawHelper(this::imageUpdateWrapper, fix -> true,
				StreamSupport.stream(matchers.spliterator(), false).toArray(FixtureMatcher[]::new));
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				leftClip = null;
				rightClip = null;
			}
		});
	}

	public Point getPoint() {
		return localPoint;
	}

	public void setPoint(final Point point) {
		localPoint = point;
		repaint();
	}

	private @Nullable Polygon leftClip = null;
	private @Nullable Polygon rightClip = null;

	private Polygon getLeftClip() {
		final Polygon local = leftClip;
		if (Objects.isNull(local)) {
			final Polygon retval = new Polygon(new int[]{getWidth() - MARGIN, MARGIN, MARGIN},
					new int[]{MARGIN, getHeight() - MARGIN, MARGIN}, 3);
			leftClip = retval;
			return retval;
		} else {
			return local;
		}
	}

	private Polygon getRightClip() {
		final Polygon local = rightClip;
		if (Objects.isNull(rightClip)) {
			final Polygon retval = new Polygon(new int[]{getWidth() - MARGIN, getWidth() - MARGIN, MARGIN},
					new int[]{MARGIN, getHeight() - MARGIN, getHeight() - MARGIN}, 3);
			rightClip = retval;
			return retval;
		} else {
			return local;
		}
	}

	@Override
	public void paintComponent(final Graphics pen) {
		super.paintComponent(pen);
		final @Nullable Shape oldClip = pen.getClip();
		final Coordinate origin = new Coordinate(0, 0);
		final Coordinate dimensions = new Coordinate(getWidth(), getHeight());
		pen.setClip(getLeftClip());
		helper.drawTile(pen, master, localPoint, origin, dimensions);
		pen.setClip(getRightClip());
		helper.drawTile(pen, subordinate, localPoint, origin, dimensions);
		pen.setClip(oldClip);
	}
}

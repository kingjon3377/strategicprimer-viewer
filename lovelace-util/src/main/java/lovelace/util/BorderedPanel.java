package lovelace.util;

import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * A {@link JPanel panel} laid out by a {@link BorderLayout}, with helper
 * methods/attributes to assign components to its different sectors. 
 */
public class BorderedPanel extends JPanel {
	private @Nullable Component center = null;

	/**
	 * The central component.
	 */
	public final @Nullable Component getCenter() {
		return center;
	}

	public final void setCenter(@Nullable final Component center) {
		if (center != null) {
			add(center, BorderLayout.CENTER);
		} else if (this.center != null) {
			remove(this.center);
		}
		this.center = center;
	}

	private @Nullable Component lineStart = null;

	/**
	 * The component in the 'line start' position; this is at the left, or
	 * 'west', in left-to-right locales.
	 */
	public final @Nullable Component getLineStart() {
		return lineStart;
	}

	public final void setLineStart(@Nullable final Component lineStart) {
		if (lineStart != null) {
			add(lineStart, BorderLayout.LINE_START);
		} else if (this.lineStart != null) {
			remove(this.lineStart);
		}
		this.lineStart = lineStart;
	}

	private @Nullable Component lineEnd = null;

	/**
	 * The component in the 'line end' position; this is at the right, or
	 * 'east', in left-to-right locales.
	 */
	public final @Nullable Component getLineEnd() {
		return lineEnd;
	}

	public final void setLineEnd(@Nullable final Component lineEnd) {
		if (lineEnd != null) {
			add(lineEnd, BorderLayout.LINE_END);
		} else if (this.lineEnd != null) {
			remove(this.lineEnd);
		}
		this.lineEnd = lineEnd;
	}

	private @Nullable Component pageStart = null;

	/**
	 * The component in the 'page start' position; this is at the top, or
	 * 'north', in left-to-right locales.
	 */
	public final @Nullable Component getPageStart() {
		return pageStart;
	}

	public final void setPageStart(@Nullable final Component pageStart) {
		if (pageStart != null) {
			add(pageStart, BorderLayout.PAGE_START);
		} else if (this.pageStart != null) {
			remove(this.pageStart);
		}
		this.pageStart = pageStart;
	}

	private @Nullable Component pageEnd = null;

	/**
	 * The component in the 'page end' position; this is at the bottom, or
	 * 'south', in left-to-right locales.
	 */
	public final @Nullable Component getPageEnd() {
		return pageEnd;
	}

	public final void setPageEnd(@Nullable final Component pageEnd) {
		if (pageEnd != null) {
			add(pageEnd, BorderLayout.PAGE_END);
		} else if (this.pageEnd != null) {
			remove(this.pageEnd);
		}
		this.pageEnd = pageEnd;
	}

	/**
	 * Constructors.
	 */
	public BorderedPanel(@Nullable final Component center, @Nullable final Component pageStart,
			@Nullable final Component pageEnd, @Nullable final Component lineEnd,
			@Nullable final Component lineStart) {
		super(new BorderLayout());
		setCenter(center);
		setPageStart(pageStart);
		setPageEnd(pageEnd);
		setLineStart(lineStart);
		setLineEnd(lineEnd);
	}

	public BorderedPanel(@Nullable final Component center, @Nullable final Component pageStart,
			@Nullable final Component pageEnd, @Nullable final Component lineEnd) {
		this(center, pageStart, pageEnd, lineEnd, null);
	}

	public BorderedPanel(@Nullable final Component center, @Nullable final Component pageStart,
			@Nullable final Component pageEnd) {
		this(center, pageStart, pageEnd, null);
	}

	public BorderedPanel(@Nullable final Component center, @Nullable final Component pageStart) {
		this(center, pageStart, null);
	}

	public BorderedPanel(@Nullable final Component center) {
		this(center, null);
	}

	public BorderedPanel() {
		this(null);
	}

	/**
	 * Factory method to arrange three components (pass null for any
	 * position to be left empty) in a vertical line.
	 */
	public static BorderedPanel verticalPanel(@Nullable final Component pageStart,
			@Nullable final Component center, @Nullable final Component pageEnd) {
		return new BorderedPanel(center, pageStart, pageEnd);
	}

	/**
	 * Factory method to arrange three components (pass null for any
	 * position to be left empty) in a horizontal line.
	 */
	public static BorderedPanel horizontalPanel(@Nullable final Component lineStart,
			@Nullable final Component center, @Nullable final Component lineEnd) {
		return new BorderedPanel(center, null, null, lineEnd,
				lineStart);
	}
}

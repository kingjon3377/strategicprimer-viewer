package lovelace.util;

import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serial;
import java.util.Objects;

/**
 * A {@link JPanel panel} laid out by a {@link BorderLayout}, with helper
 * methods/attributes to assign components to its different sectors.
 */
@SuppressWarnings("ClassWithTooManyConstructors")
public class BorderedPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;
	private @Nullable Component center = null;

	/**
	 * The central component.
	 */
	public final @Nullable Component getCenter() {
		return center;
	}

	public final void setCenter(final @Nullable Component center) {
		if (Objects.nonNull(center)) {
			add(center, BorderLayout.CENTER);
		} else if (Objects.nonNull(this.center)) {
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

	public final void setLineStart(final @Nullable Component lineStart) {
		if (Objects.nonNull(lineStart)) {
			add(lineStart, BorderLayout.LINE_START);
		} else if (Objects.nonNull(this.lineStart)) {
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

	public final void setLineEnd(final @Nullable Component lineEnd) {
		if (Objects.nonNull(lineEnd)) {
			add(lineEnd, BorderLayout.LINE_END);
		} else if (Objects.nonNull(this.lineEnd)) {
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

	public final void setPageStart(final @Nullable Component pageStart) {
		if (Objects.nonNull(pageStart)) {
			add(pageStart, BorderLayout.PAGE_START);
		} else if (Objects.nonNull(this.pageStart)) {
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

	public final void setPageEnd(final @Nullable Component pageEnd) {
		if (Objects.nonNull(pageEnd)) {
			add(pageEnd, BorderLayout.PAGE_END);
		} else if (Objects.nonNull(this.pageEnd)) {
			remove(this.pageEnd);
		}
		this.pageEnd = pageEnd;
	}

	/**
	 * Constructors.
	 */
	public BorderedPanel(final @Nullable Component center, final @Nullable Component pageStart,
						 final @Nullable Component pageEnd, final @Nullable Component lineEnd,
						 final @Nullable Component lineStart) {
		super(new BorderLayout());
		setCenter(center);
		setPageStart(pageStart);
		setPageEnd(pageEnd);
		setLineStart(lineStart);
		setLineEnd(lineEnd);
	}

	public BorderedPanel(final @Nullable Component center, final @Nullable Component pageStart,
						 final @Nullable Component pageEnd, final @Nullable Component lineEnd) {
		this(center, pageStart, pageEnd, lineEnd, null);
	}

	public BorderedPanel(final @Nullable Component center, final @Nullable Component pageStart,
						 final @Nullable Component pageEnd) {
		this(center, pageStart, pageEnd, null);
	}

	public BorderedPanel(final @Nullable Component center, final @Nullable Component pageStart) {
		this(center, pageStart, null);
	}

	public BorderedPanel(final @Nullable Component center) {
		this(center, null);
	}

	public BorderedPanel() {
		this(null);
	}

	/**
	 * Factory method to arrange three components (pass null for any
	 * position to be left empty) in a vertical line.
	 */
	public static BorderedPanel verticalPanel(final @Nullable Component pageStart,
											  final @Nullable Component center, final @Nullable Component pageEnd) {
		return new BorderedPanel(center, pageStart, pageEnd);
	}

	/**
	 * Factory method to arrange three components (pass null for any
	 * position to be left empty) in a horizontal line.
	 */
	public static BorderedPanel horizontalPanel(final @Nullable Component lineStart,
												final @Nullable Component center, final @Nullable Component lineEnd) {
		return new BorderedPanel(center, null, null, lineEnd,
				lineStart);
	}

	@Override
	public final String toString() {
		return "BorderedPanel{center=%s, lineStart=%s, lineEnd=%s, pageStart=%s, pageEnd=%s}".formatted(center,
				lineStart, lineEnd, pageStart, pageEnd);
	}
}

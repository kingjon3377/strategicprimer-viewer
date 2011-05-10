package view.map.main;

import java.awt.Dimension;
import java.awt.event.MouseListener;

/**
 * A "piece" to represent a unit or fortress (or something else ...) in the
 * details-panel.
 * 
 * @author kingjon
 * 
 */
public abstract class Chit extends Selectable { // NOPMD
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 6370080411961068523L;

	/**
	 * @return the description of the unit or fortress this chit represents, to
	 *         report to the user
	 */
	public abstract String describe();

	/**
	 * The maximum size of a chit.
	 */
	private static final int SIZE = 20;
	/**
	 * The maximum size of a chit, in the form other objects will want.
	 */
	private static final Dimension MAX_SIZE = new Dimension(SIZE, SIZE);

	/**
	 * @return the maximum size of a chit
	 */
	@Override
	public Dimension getMaximumSize() {
		return MAX_SIZE;
	}

	/**
	 * @return the minimum size of a chit
	 */
	@Override
	public Dimension getMinimumSize() {
		return MAX_SIZE;
	}

	/**
	 * @return the preferred size of a chit
	 */
	@Override
	public Dimension getPreferredSize() {
		return MAX_SIZE;
	}

	/**
	 * Constructor.
	 * 
	 * @param listener
	 *            a listener to detect clicks on the chit.
	 */
	protected Chit(final MouseListener listener) {
		super();
		setOpaque(false);
		addMouseListener(listener);
		// setBorder(new EmptyBorder(5, 5, 5, 5));
	}
}

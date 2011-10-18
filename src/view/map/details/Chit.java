package view.map.details;

import java.awt.Dimension;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.MouseListener;

import view.map.main.Selectable;

/**
 * A "piece" to represent a unit or fortress (or something else ...) in the
 * details-panel.
 * 
 * @author Jonathan Lovelace
 * 
 */
public abstract class Chit extends Selectable { // NOPMD
	/**
	 * A wrapper around our data.
	 */
	private final Transferable trans;

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
	 * @param transferable
	 *            a Transferable encapsulating the data the Chit represents.
	 */
	protected Chit(final MouseListener listener, final Transferable transferable) {
		super();
		setOpaque(false);
		addMouseListener(listener);
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
				this, DnDConstants.ACTION_COPY,
				new ChitDragGestureListener());
		// setBorder(new EmptyBorder(5, 5, 5, 5));
		trans = transferable;
	}
	/**
	 * @return a Transferable encapsulating the fortress this represents
	 */
	public Transferable getData() {
		return trans;
	}
}

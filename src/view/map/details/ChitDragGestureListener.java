package view.map.details;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;

/**
 * A listener to start chit drags.
 * 
 * @author Jonathan Lovelace
 */
public class ChitDragGestureListener implements DragGestureListener {
	/**
	 * Constructor.
	 * 
	 * @param data
	 *            the data to transfer
	 */
	public ChitDragGestureListener(final Transferable data) {
		trans = data;
	}

	/**
	 * The data to transfer if a drag is initiated.
	 */
	private final Transferable trans;

	/**
	 * Start a drag if the operation is appropriate.
	 * 
	 * @param dge
	 *            the event to handle.
	 */
	@Override
	public void dragGestureRecognized(final DragGestureEvent dge) {
		dge.startDrag(null, trans);
	}

	/**
	 * 
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ChitDragGestureListener";
	}
}

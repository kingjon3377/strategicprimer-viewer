package view.map.details;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
/**
 * A listener to start chit drags.
 * @author Jonathan Lovelace
 */
public class ChitDragGestureListener implements DragGestureListener {
	/**
	 * Start a drag if the operation is appropriate.
	 * @param dge the event to handle.
	 */
	@Override
	public void dragGestureRecognized(final DragGestureEvent dge) {
		if (dge.getComponent() instanceof Chit) {
			dge.startDrag(null, ((Chit) dge.getComponent()).getData());
		}
	}

}

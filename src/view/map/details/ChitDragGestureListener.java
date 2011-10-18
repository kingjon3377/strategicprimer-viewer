package view.map.details;

import java.awt.datatransfer.Transferable;
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
			Chit chit = (Chit) dge.getComponent();
			Transferable transferable = chit.getData();
			dge.startDrag(null, transferable);
		}
	}

}

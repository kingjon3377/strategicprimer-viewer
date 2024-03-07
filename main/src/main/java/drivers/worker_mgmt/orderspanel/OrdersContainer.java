package drivers.worker_mgmt.orderspanel;

import drivers.worker_mgmt.Applyable;
import drivers.worker_mgmt.Revertible;

import javax.swing.event.TreeSelectionListener;

import drivers.common.PlayerChangeListener;

public interface OrdersContainer
		extends Applyable, Revertible, TreeSelectionListener, PlayerChangeListener {
	/**
	 * If the given string is present (ignoring case) in the orders or
	 * results text area, cause that string to become selected in the text
	 * and return true; otherwise, return false.
	 */
	boolean selectText(String substring);
}

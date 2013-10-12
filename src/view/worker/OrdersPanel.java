package view.worker;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jdt.annotation.Nullable;

import model.map.fixtures.mobile.Unit;
import view.util.ApplyButtonHandler;
import view.util.Applyable;
import view.util.BorderedPanel;
import view.util.ListenedButton;
/**
 * A panel for the user to enter a unit's orders.
 * @author Jonathan Lovelace
 *
 */
public class OrdersPanel extends BorderedPanel implements Applyable, TreeSelectionListener {
	/**
	 * The text area in which the user writes the orders.
	 */
	private final JTextArea area = new JTextArea();
	/**
	 * Constructor.
	 */
	public OrdersPanel() {
		final ApplyButtonHandler handler = new ApplyButtonHandler(this);
		// Can't use the multi-arg constructor, because of the references to 'this' below.
		setNorth(new JLabel("Orders for current selection, if a unit:"))
				.setCenter(new JScrollPane(area)).setSouth(
						new BorderedPanel().setLineStart(
								new ListenedButton("Apply", handler))
								.setLineEnd(
										new ListenedButton("Revert", handler)));
	}
	/**
	 * The current selection.
	 */
	@Nullable private Object sel;
	/**
	 * If a unit is selected, change its orders to what the user wrote.
	 */
	@Override
	public void apply() {
		if (sel instanceof Unit) {
			((Unit) sel).setOrders(area.getText().trim());
		}
	}

	/**
	 * Change the text in the area to either the current orders, if a unit is
	 * selected, or the empty string, if one is not.
	 */
	@Override
	public void revert() {
		if (sel instanceof Unit) {
			area.setText(((Unit) sel).getOrders().trim());
		} else {
			area.setText("");
		}
	}
	/**
	 * @param evt the event to handle
	 */
	@Override
	public void valueChanged(@Nullable final TreeSelectionEvent evt) {
		if (evt != null) {
			sel = evt.getNewLeadSelectionPath().getLastPathComponent();
			if (sel instanceof DefaultMutableTreeNode) {
				sel = ((DefaultMutableTreeNode) sel).getUserObject();
			}
			revert();
		}
	}

}

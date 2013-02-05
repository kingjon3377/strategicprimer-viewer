package view.map.details;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import model.map.TileFixture;
import model.viewer.CurriedFixtureTransferable;
import model.viewer.FixtureListDropListener;
import model.viewer.FixtureListModel;
import model.viewer.FixtureTransferable;
import util.PropertyChangeSource;

/**
 * A visual list-based representation of the contents of a tile.
 *
 * @author Jonathan Lovelace
 */
public class FixtureList extends JList<TileFixture> implements
		DragGestureListener {
	/**
	 * Constructor.
	 *
	 * @param sources objects the model should listen to
	 * @param parent a parent of this list
	 */
	public FixtureList(final JComponent parent, final PropertyChangeSource... sources) {
		super(new FixtureListModel(sources));
		setCellRenderer(new FixtureCellRenderer());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
				this, DnDConstants.ACTION_COPY, this);
		setDropTarget(new DropTarget(this, new FixtureListDropListener(parent,
				(FixtureListModel) getModel())));
		final InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete");
		getActionMap().put("delete", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				((FixtureListModel) getModel()).remove(getSelectedValuesList());
			}
		});
	}
	/**
	 * Start a drag when appropriate.
	 * @param dge the event to handle
	 */
	@Override
	public void dragGestureRecognized(final DragGestureEvent dge) {
		final List<TileFixture> selection = getSelectedValuesList();
		final Transferable trans = selection.size() == 1 ? new FixtureTransferable(
				selection.get(0))
				: new CurriedFixtureTransferable(selection);
		dge.startDrag(null, trans);
	}

	/**
	 * A FixtureList is equal to only another JList with the same model. If obj
	 * is a DropTarget, we compare to its Component.
	 *
	 * @param obj another object
	 * @return whether it's equal to this one
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof JList
						&& getModel().equals(((JList) obj).getModel()));
	}
	/**
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return getModel().hashCode();
	}
}

package model.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import model.map.Tile;
import model.map.TileFixture;
import util.PropertyChangeSource;
/**
 * A model for a FixtureTree.
 *
 * @author Jonathan Lovelace
 */
public class FixtureTreeModel implements PropertyChangeListener, TreeModel {
	/**
	 * Listeners.
	 */
	private final EventListenerList listeners = new EventListenerList();
	/**
	 * The property we listen for.
	 */
	private final String listenedProperty;
	/**
	 * The current tile.
	 */
	private TreeNode tile = new TileTreeNode();
	/**
	 * Constructor.
	 *
	 * @param property The property to listen for to get the new tile
	 * @param sources sources to listen to
	 */
	public FixtureTreeModel(final String property,
			final PropertyChangeSource... sources) {
		super();
		listenedProperty = property;
		for (final PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(this);
		}
	}

	/**
	 * Handle a property change.
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (listenedProperty.equalsIgnoreCase(evt.getPropertyName())
				&& evt.getNewValue() instanceof Tile) {
			tile = new TileTreeNode((Tile) evt.getNewValue());
			for (final TreeModelListener listener : listeners
					.getListeners(TreeModelListener.class)) {
				listener.treeStructureChanged(new TreeModelEvent(this,
						new TreePath(tile)));
			}
		}
	}
	/**
	 * @return the root of the tree, a node representing the current tile
	 */
	@Override
	public Object getRoot() {
		return tile;
	}
	/**
	 * @param parent a parent node
	 * @param index the index of a child under it
	 * @return that child
	 */
	@Override
	public Object getChild(final Object parent, final int index) {
		if (parent instanceof TreeNode) {
			return ((TreeNode) parent).getChildAt(index); // NOPMD
		} else {
			return null;
		}
	}
	/**
	 * @param parent a parent
	 * @return how many children it has
	 */
	@Override
	public int getChildCount(final Object parent) {
		if (parent instanceof TreeNode) {
			return ((TreeNode) parent).getChildCount(); // NOPMD
		} else {
			return 0;
		}
	}
	/**
	 * @param node a node
	 * @return whether it's a leaf
	 */
	@Override
	public boolean isLeaf(final Object node) {
		if (node instanceof TreeNode) {
			return ((TreeNode) node).isLeaf(); // NOPMD
		} else {
			return true;
		}
	}
	/**
	 * FIXME: This method does nothing, because I can't figure out what it's supposed to do.
	 * @param path the path to a node
	 * @param newValue its new value
	 */
	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		// Nothing ...
	}
	/**
	 * @param parent a parent node
	 * @param child a child node
	 * @return the index of the child on the parent
	 */
	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		if (parent instanceof TreeNode && child instanceof TreeNode) {
			return ((TreeNode) parent).getIndex((TreeNode) child); // NOPMD
		} else {
			return -1;
		}
	}
	/**
	 * Add a listener.
	 * @param listener the listener to add
	 */
	@Override
	public void addTreeModelListener(final TreeModelListener listener) {
		listeners.add(TreeModelListener.class, listener);
	}
	/**
	 * Remove a listener.
	 * @param listener the listener to remove
	 */
	@Override
	public void removeTreeModelListener(final TreeModelListener listener) {
		listeners.remove(TreeModelListener.class, listener);
	}

}

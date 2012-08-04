package model.viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import model.map.Tile;
import model.map.TileFixture;
/**
 * A node in a tree representing a tile.
 * @author Jonathan Lovelace
 */
public class TileNode implements TreeNode {
	/**
	 * A string describing the tile.
	 */
	private final String string;
	/**
	 * Constructor.
	 * @param tile the tile this represents.
	 */
	public TileNode(final Tile tile) {
		for (TileFixture fix : tile.getContents()) {
			// TODO: Specialized nodes for editable fixtures 
			children.add(new FixtureNode(fix, this)); // NOPMD
		}
		string = new StringBuilder(tile.getLocation().toString())
				.append(": ").append(tile.getTerrain().toString()).toString();
	}
	/**
	 * The children of this node.
	 */
	private final List<TreeNode> children = new ArrayList<TreeNode>(); 
	/**
	 * @param childIndex
	 *            the index of a child
	 * @return the child at that index.
	 */
	@Override
	public TreeNode getChildAt(final int childIndex) {
		return children.get(childIndex);
	}
	/**
	 * @return how many children the node has.
	 */
	@Override
	public int getChildCount() {
		return children.size();
	}
	/**
	 * @return null, since a tile has no parent
	 * @see javax.swing.tree.TreeNode#getParent()
	 */
	@Override
	public TreeNode getParent() {
		return null;
	}
	/**
	 * @param node a node
	 * @return its index under this one, or -1 if it's not an immediate child.
	 */
	@Override
	public int getIndex(final TreeNode node) {
		return children.indexOf(node);
	}
	/**
	 * @return true: a Tile can have children.
	 */
	@Override
	public boolean getAllowsChildren() { // NOPMD
		return true;
	}
	/**
	 * @return false: a Tile is not a leaf node.
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}
	/**
	 * @return an Enumeration of the children.
	 */
	@Override
	public Enumeration children() {
		return Collections.enumeration(children);
	}
	/**
	 * @return a string describing the tile.
	 */
	public String getTileString() {
		return string;
	}

}

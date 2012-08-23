package model.viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
/**
 * A TreeNode wrapping a Tile.
 * @author Jonathan Lovelace
 */
public class TileTreeNode implements TreeNode {
	/**
	 * Default constructor, for use before the tree has a root.
	 */
	public TileTreeNode() {
		this(new Tile(-1, -1, TileType.NotVisible, "string"));
	}
	/**
	 * Constructor.
	 * @param wrapped the tile we wrap.
	 */
	public TileTreeNode(final Tile wrapped) {
//		tile = wrapped;
		for (TileFixture fix : wrapped) {
			contents.add(TreeNodeFactory.create(this, fix));
		}
	}
//	/**
//	 * The tile we wrap.
//	 */
//	private final Tile tile;

	/**
	 * A list of fixtures on the tile. FIXME: Either reverse the recent changes
	 * hiding the details of Tile's contents, or figure out some way for this to
	 * get updated when that collection changes.
	 */
	private final List<TreeNode> contents = new ArrayList<TreeNode>();
	/**
	 * @param index the index of a child
	 * @return the child at that index
	 */
	@Override
	public TreeNode getChildAt(final int index) {
		return contents.get(index);
	}
	/**
	 * @return how many children the node has
	 */
	@Override
	public int getChildCount() {
		return contents.size();
	}
	/**
	 * @return null: a tile has no parent
	 */
	@Override
	public TreeNode getParent() {
		return null;
	}
	/**
	 * @param node a node
	 * @return its index under us
	 */
	@Override
	public int getIndex(final TreeNode node) {
		return contents.indexOf(node);
	}
	/**
	 * @return true: a Tile can have contents
	 */
	@Override
	public boolean getAllowsChildren() { // NOPMD
		return true;
	}
	/**
	 * @return false: a Tile is not a leaf node
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}
	/**
	 * @return an enumeration of our children
	 */
	@Override
	public Enumeration children() {
		return Collections.enumeration(contents);
	}
}

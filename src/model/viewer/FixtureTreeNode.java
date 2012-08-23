package model.viewer;

import java.util.Collections;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import model.map.TileFixture;
/**
 * A general TreeNode implementation wrapping a TileFixture.
 * @author Jonathan Lovelace
 *
 */
public class FixtureTreeNode implements TreeNode {
	/**
	 * Constructor.
	 * @param parentNode the parent node
	 * @param fix the fixture this wraps.
	 */
	public FixtureTreeNode(final TreeNode parentNode, final TileFixture fix) {
		parent = parentNode;
		fixture = fix;
	}
	/**
	 * The fixture this wraps.
	 */
	private final TileFixture fixture;
	/**
	 * @return the fixture this wraps
	 */
	public TileFixture getFixture() {
		return fixture;
	}
	/**
	 * The parent of this node.
	 */
	private final TreeNode parent;
	/**
	 * @param childIndex ignored
	 * @return null: this is a leaf
	 */
	@Override
	public TreeNode getChildAt(final int childIndex) {
		return null;
	}
	/**
	 * @return 0: this is a leaf
	 */
	@Override
	public int getChildCount() {
		return 0;
	}
	/**
	 * @return the parent of this node
	 */
	@Override
	public TreeNode getParent() {
		return parent;
	}
	/**
	 * @param node ignored
	 * @return -1: this is a leaf node
	 */
	@Override
	public int getIndex(final TreeNode node) {
		return -1;
	}
	/**
	 * @return false: this node can't have children.
	 */
	@Override
	public boolean getAllowsChildren() { // NOPMD
		return false;
	}
	/**
	 * @return true: this is a leaf node
	 */
	@Override
	public boolean isLeaf() {
		return true;
	}
	/**
	 * @return an empty enumeration
	 */
	@Override
	public Enumeration children() {
		return Collections.emptyEnumeration();
	}
}

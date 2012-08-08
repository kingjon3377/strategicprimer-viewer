package model.viewer;

import java.util.Collections;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import model.map.TileFixture;

/**
 * A TreeNode representing a TileFixture. Subclasses should be used for fixtures
 * with details that should show up in the tree.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class FixtureNode implements TreeNode {
	/**
	 * The parent node.
	 */
	private final TreeNode parent;
	/**
	 * The fixture this represents.
	 */
	private final TileFixture fixture;

	/**
	 * @return the fixture this represents
	 */
	public TileFixture getFixture() {
		return fixture;
	}

	/**
	 * Constructor.
	 * 
	 * @param parentNode the parent node.
	 * @param fix the fixture this represents.
	 */
	public FixtureNode(final TileFixture fix, final TreeNode parentNode) {
		parent = parentNode;
		fixture = fix;
	}

	/**
	 * 
	 * @param childIndex ignored
	 * @return null, as there are no children
	 */
	@Override
	public TreeNode getChildAt(final int childIndex) {
		return null;
	}

	/**
	 * @return 0, as we have no children
	 */
	@Override
	public int getChildCount() {
		return 0;
	}

	/**
	 * @return the parent node
	 */
	@Override
	public TreeNode getParent() {
		return parent;
	}

	/**
	 * @param node ignored
	 * @return -1, as we have no children
	 */
	@Override
	public int getIndex(final TreeNode node) {
		return -1;
	}

	/**
	 * @return false: we don't allow children.
	 */
	@Override
	public boolean getAllowsChildren() { // NOPMD
		return false;
	}

	/**
	 * @return true: this is a leaf node.
	 */
	@Override
	public boolean isLeaf() {
		return true;
	}

	/**
	 * @return an empty enumeration: we have no children.
	 */
	@Override
	public Enumeration children() {
		return Collections.emptyEnumeration();
	}
}

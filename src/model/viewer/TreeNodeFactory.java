package model.viewer;

import javax.swing.tree.TreeNode;

import model.map.TileFixture;

/**
 * A factory for TreeNodes.
 * @author Jonathan Lovelace
 */
public final class TreeNodeFactory {
	/**
	 * Do not instantiate.
	 */
	private TreeNodeFactory() {
		// Do nothing
	}
	/**
	 * Create a node for a TileFixture.
	 * @param parent the node's parent node
	 * @param fix the fixture to create a node to wrap.
	 * @return such a node
	 */
	public static TreeNode create(final TreeNode parent, final TileFixture fix) {
		// TODO: special-case for fortresses and other parents.
		return new FixtureTreeNode(parent, fix);
	}
}

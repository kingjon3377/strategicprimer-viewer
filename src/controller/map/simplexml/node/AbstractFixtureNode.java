package controller.map.simplexml.node;

import model.map.TileFixture;

/**
 * A class to simplify TileNode's handling of its children. This shouldn't ever
 * be directly instantiated.
 * 
 * @author Jonathan Lovelace
 * @param <T> The Fixture type this will produce.
 * 
 */
//ESCA-JAVA0024:
//ESCA-JAVA0011:
public abstract class AbstractFixtureNode<T extends TileFixture> extends AbstractChildNode<T> { // NOPMD
	// This is a superclass to unite its subclasses, and abstract to avoid
	// pointless code implementing the methods required by its superclass.
	/**
	 * Constructor.
	 * @param type the type of child we're to produce
	 */
	protected AbstractFixtureNode(final Class<T> type) {
		super(type);
	}
}

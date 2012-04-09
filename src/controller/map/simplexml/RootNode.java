package controller.map.simplexml;

import java.util.Iterator;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.node.AbstractChildNode;
import controller.map.simplexml.node.AbstractXMLNode;

/**
 * A node at the root of the hierarchy. Its only child should be a ChildNode
 * producing the type we want.
 * 
 * @author Jonathan Lovelace
 * @param <T>
 *            The kind of child we want.
 * 
 */
public final class RootNode<T> extends AbstractXMLNode {
	/**
	 * Check whether the tree is valid. Since we can't check whether it has more
	 * than one child, we only verify that it has at least one, which is the
	 * child we want.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if it isn't.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			final AbstractXMLNode child = iterator().next();
			if (child instanceof AbstractChildNode) {
				if (((AbstractChildNode) child).getProduct().isAssignableFrom(product)) {
					iterator().next().checkNode(warner);
				} else {
					throw new SPFormatException("We want a node producing "
							+ product.getSimpleName()
							+ " as the top-level tag, not one producing "
							+ ((AbstractChildNode) child).getProduct()
									.getSimpleName(), 0);
				}
			} else {
				throw new SPFormatException("We want a node producing "
						+ product.getSimpleName() + " as the top-level tag", 0);
			}
		} else {
			throw new SPFormatException("We want a node producing "
					+ product.getSimpleName() + " as the top-level tag", 0);
		}
	}

	/**
	 * @return the root node, which should be our only child.
	 * @throws SPFormatException
	 *             if we don't have a child or it isn't what we wanted.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AbstractChildNode<T> getRootNode() throws SPFormatException {
		final Iterator<AbstractXMLNode> iterator = iterator();
		if (iterator.hasNext()) {
			final AbstractXMLNode child = iterator.next();
			if (child instanceof AbstractChildNode
					&& ((AbstractChildNode) child).getProduct().isAssignableFrom(product)) {
				return (AbstractChildNode<T>) child;
			} else {
				throw new SPFormatException(
						"First top-level tag won't produce a "
								+ product.getSimpleName(), 0);
			}
		} else {
			throw new SPFormatException("No top-level tag", 0);
		}
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "RootNode";
	}

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            the type of child we want to produce.
	 */
	public RootNode(final Class<T> type) {
		super();
		product = type;
	}

	/**
	 * The type of child we want to produce.
	 */
	private final Class<T> product;
}

package controller.map.simplexml;

import java.util.Iterator;

import model.map.MapView;
import model.map.PlayerCollection;
import model.map.SPMap;
import util.Warning;
import controller.map.MissingChildException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
import controller.map.simplexml.node.AbstractChildNode;
import controller.map.simplexml.node.AbstractXMLNode;
import controller.map.simplexml.node.ViewNode;

/**
 * A node at the root of the hierarchy. Its only child should be a ChildNode
 * producing the type we want.
 *
 * @author Jonathan Lovelace
 * @param <T> The kind of child we want.
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public final class RootNode<T> extends AbstractXMLNode {
	/**
	 * Check whether the tree is valid. Since we can't check whether it has more
	 * than one child, we only verify that it has at least one, which is the
	 * child we want.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if it isn't.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		if (iterator().hasNext()) {
			final AbstractXMLNode child = iterator().next();
			if (child instanceof AbstractChildNode) {
				if (((AbstractChildNode) child).getProduct().isAssignableFrom(
						product)
						|| (product.equals(MapView.class) && ((AbstractChildNode) child)
								.getProduct().equals(SPMap.class))) {
					iterator().next().checkNode(warner, idFactory);
				} else {
					throw new IllegalArgumentException(
							"We want a node producing "
									+ product.getSimpleName()
									+ " as the top-level tag, not one producing "
									+ ((AbstractChildNode) child).getProduct()
											.getSimpleName());
				}
			} else {
				throw new IllegalArgumentException("We want a node producing "
						+ product.getSimpleName() + " as the top-level tag");
			}
		} else {
			throw new IllegalArgumentException("We want a node producing "
					+ product.getSimpleName() + " as the top-level tag");
		}
	}

	/**
	 * @return the root node, which should be our only child.
	 * @throws SPFormatException if we don't have a child or it isn't what we
	 *         wanted.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AbstractChildNode<T> getRootNode() throws SPFormatException {
		final Iterator<AbstractXMLNode> iterator = iterator();
		if (iterator.hasNext()) {
			final AbstractXMLNode child = iterator.next();
			if (child instanceof AbstractChildNode
					&& (((AbstractChildNode) child).getProduct()
							.isAssignableFrom(product))) {
				return (AbstractChildNode<T>) child; // NOPMD
			} else if (child instanceof AbstractChildNode
					&& product.equals(MapView.class)
					&& ((AbstractChildNode) child).getProduct().equals(
							SPMap.class)) {
				final AbstractChildNode<MapView> root = new ViewNode();
				root.addChild(child);
				// FIXME: We "deserialize" from the intermediate-representation
				// twice, because we can't get at the child's properties from
				// here.
				root.addProperty(
						"current_player",
						Integer.toString(((AbstractChildNode<SPMap>) child)
								.produce(new PlayerCollection(),
										new Warning(Warning.Action.Die))
								.getPlayers().getCurrentPlayer().getPlayerId()),
						new Warning(Warning.Action.Die));
				root.addProperty("current_turn", "0", new Warning(
						Warning.Action.Die));
				return (AbstractChildNode<T>) root;
			} else {
				throw new IllegalArgumentException(
						"First top-level tag won't produce a "
								+ product.getSimpleName());
			}
		} else {
			throw new MissingChildException("root", 0);
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
	 * @param type the type of child we want to produce.
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

package controller.map.simplexml.node;

import java.util.Iterator;

import model.map.PlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMap;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingChildException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A node to produce submaps for map views.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
class SubmapNode extends AbstractChildNode<SubmapNode.Submap> {
	/**
	 * The tag we're dealing with.
	 */
	private static final String TAG = "submap";
	/**
	 * A class to represent a submap.
	 */
	static class Submap {
		/**
		 * The submap itself.
		 */
		private final SPMap map;
		/**
		 * The location of the tile it represents.
		 */
		private final Point location;
		/**
		 * @return the submap
		 */
		public SPMap getMap() {
			return map;
		}
		/**
		 * @return the location of the tile the submap represents
		 */
		public Point getLocation() {
			return location;
		}
		/**
		 * Constructor.
		 * @param submap the submap
		 * @param point its location
		 */
		// ESCA-JAVA0128:
		public Submap(final Point point, final SPMap submap) {
			location = point;
			map = submap;
		}
	}
	/**
	 * Constructor.
	 */
	public SubmapNode() {
		super(Submap.class);
	}
	/**
	 * Produce an object encapsulating the submap.
	 * @param players the player collection to use
	 * @param warner the warner to use
	 * @return the object produced
	 * @throws SPFormatException on format error
	 */
	@Override
	public Submap produce(final PlayerCollection players,
			final Warning warner) throws SPFormatException {
		return new Submap(PointFactory.point(
				Integer.parseInt(getProperty("row")),
				Integer.parseInt(getProperty("column"))), ((MapNode) iterator()
				.next()).produce(players, warner));
	}
	/**
	 * @param property a property
	 * @return whether we can use it
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, "row", "column");
	}
	/**
	 * Check the node for errors.
	 * @param warner the Warning instance to use.
	 * @param idFactory the ID factory to use.
	 * @throws SPFormatException on format problems
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final Iterator<AbstractXMLNode> iter = iterator();
		if (!iter.hasNext()) {
			throw new MissingChildException(TAG, getLine());
		} 
		final AbstractXMLNode child = iter.next();
		if (child instanceof AbstractChildNode
				&& SPMap.class.isAssignableFrom(((AbstractChildNode<?>) child)
						.getProduct())) {
			child.checkNode(warner, idFactory);
		} else {
			throw new UnwantedChildException(TAG, child.toString(), getLine());
		}
		if (iter.hasNext()) {
			throw new UnwantedChildException(TAG, child.toString(), getLine());
		}
		demandProperty(TAG, "row", warner, false, false);
		demandProperty(TAG, "column", warner, false, false);
	}
}

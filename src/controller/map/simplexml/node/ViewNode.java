package controller.map.simplexml.node;

import model.map.MapView;
import model.map.PlayerCollection;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingChildException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A node to produce a MapView. TODO: changesets.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class ViewNode extends AbstractChildNode<MapView> {
	/**
	 * The tag we're primarily dealing with.
	 */
	private static final String TAG = "view";

	/**
	 * Constructor.
	 */
	public ViewNode() {
		super(MapView.class);
	}

	/**
	 * Produce the view. TODO: changesets.
	 *
	 * @param players the players object to pass down
	 * @param warner the Warning instance to use
	 * @return the view this represents
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public MapView produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		MapNode map = null;
		for (final AbstractXMLNode child : this) {
			if (child instanceof MapNode) {
				map = (MapNode) child;
				break;
			}
		}
		if (map == null) {
			throw new MissingChildException(TAG, getLine());
		}
		final MapView retval = new MapView(map.produce(players, warner),
				Integer.parseInt(getProperty("current_player")),
				Integer.parseInt(getProperty("current_turn")),
				getProperty("file"));
		return retval;
	}

	/**
	 * @param property a property
	 * @return whether we can use it.
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, "current_player", "current_turn");
	}

	/**
	 * Check for errors. TODO: changesets.
	 *
	 * @param warner the Warning instance to use.
	 * @param idFactory the ID factory to pass down.
	 * @throws SPFormatException on format problems.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		int mapnodes = 0;
		if (!iterator().hasNext()) {
			throw new MissingChildException(TAG, getLine());
		}
		for (final AbstractXMLNode node : this) {
			if (mapnodes == 0 && node instanceof MapNode) {
				node.checkNode(warner, idFactory);
				mapnodes++;
			} else {
				throw new UnwantedChildException(TAG, node.toString(),
						getLine());
			}
		}
		demandProperty(TAG, "current_player", warner, false, false);
		demandProperty(TAG, "current_turn", warner, false, false);
	}

	/**
	 * @return a String representation of the Node.
	 */
	@Override
	public String toString() {
		return "ViewNode";
	}
}

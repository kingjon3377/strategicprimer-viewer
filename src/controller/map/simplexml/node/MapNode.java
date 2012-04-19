package controller.map.simplexml.node;

import java.util.LinkedList;
import java.util.List;

import model.map.PlayerCollection;
import model.map.SPMap;
import util.EqualsAny;
import util.Warning;
import controller.map.MapVersionException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A node generated from the <map> tag.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MapNode extends AbstractChildNode<SPMap> {
	/**
	 * The name of the property saying how many rows.
	 */
	private static final String ROWS_PROPERTY = "rows";
	/**
	 * The name of the property saying how many columns.
	 */
	private static final String COLUMNS_PROPERTY = "columns";
	/**
	 * The tag.
	 */
	private static final String TAG = "map";

	/**
	 * Constructor.
	 */
	public MapNode() {
		super(SPMap.class);
	}
	/**
	 * The (name of the) "version" property.
	 */
	private static final String VERSION_PROP = "version";

	/**
	 * Check the node. A Map is valid iff every child is either a Player or a
	 * Tile and it includes version (greater than or equal to 1, for this
	 * version of the reader), rows, and columns properties.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if all is not well.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		for (final AbstractXMLNode node : this) {
			if (node instanceof TileNode || node instanceof PlayerNode) {
				node.checkNode(warner);
			} else {
				throw new UnwantedChildException(TAG, node.toString(),
						getLine());
			}
		}
		if (!hasProperty(VERSION_PROP)
				|| Integer.parseInt(getProperty(VERSION_PROP)) < SPMap.MAX_VERSION) {
			throw new MapVersionException(
					"This reader only accepts maps with a \"version\" property of at least "
							+ SPMap.MAX_VERSION, getLine());
		} else if (hasProperty(ROWS_PROPERTY)) {
			if (!hasProperty(COLUMNS_PROPERTY)) {
				throw new MissingParameterException(TAG, COLUMNS_PROPERTY, getLine());
			}
		} else {
			throw new MissingParameterException(TAG, ROWS_PROPERTY, getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, VERSION_PROP, ROWS_PROPERTY, COLUMNS_PROPERTY, "current_player");
	}
	
	/**
	 * 
	 * @param players
	 *            will be null, and is ignored
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @return the map the XML represented
	 * @throws SPFormatException
	 *             if something's wrong with the format.
	 */
	@Override
	public SPMap produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final SPMap map = new SPMap(Integer.parseInt(getProperty(VERSION_PROP)),
				Integer.parseInt(getProperty(ROWS_PROPERTY)),
				Integer.parseInt(getProperty(COLUMNS_PROPERTY)));
		final List<TileNode> tiles = new LinkedList<TileNode>();
		for (final AbstractXMLNode node : this) {
			if (node instanceof PlayerNode) {
				map.addPlayer(((PlayerNode) node).produce(players, warner));
			} else if (node instanceof TileNode) {
				tiles.add((TileNode) node);
			} 
		}
		for (final TileNode node : tiles) {
			map.addTile(node.produce(map.getPlayers(), warner));
		}
		if (hasProperty("current_player")) {
			map.getPlayers()
					.getPlayer(Integer.parseInt(getProperty("current_player")))
					.setCurrent(true);
		}
		return map;
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapNode";
	}
}

package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.TextFixture;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.simplexml.ITextNode;

/**
 * A Node to represent a Tile.
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
public class TileNode extends AbstractChildNode<Tile> implements ITextNode {
	/**
	 * The deprecated version of KIND_PROPERTY.
	 */
	private static final String OLD_KIND_PROPERTY = "type";
	/**
	 * The name of the property saying what column this is.
	 */
	private static final String COL_PROPERTY = "column";
	/**
	 * The name of the property saying what row this is.
	 */
	private static final String ROW_PROPERTY = "row";
	/**
	 * The name of the terrain-type property.
	 */
	private static final String TERRAIN_PROPERTY = "kind";

	/**
	 * Constructor.
	 */
	public TileNode() {
		super(Tile.class);
	}

	/**
	 * Produce the equivalent Tile.
	 * 
	 * @param players the players in the map
	 * @param warner a Warning instance to use for warnings
	 * @return the equivalent Tile.
	 * @throws SPFormatException if we contain invalid data.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Tile produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Tile tile = new Tile(Integer.parseInt(getProperty(ROW_PROPERTY)),
				Integer.parseInt(getProperty(COL_PROPERTY)),
				TileType.getTileType(getProperty(TERRAIN_PROPERTY)),
				getProperty("file"));
		for (final AbstractXMLNode node : this) {
			if (node instanceof RiverNode) {
				tile.addRiver(((RiverNode) node).produce(players, warner));
			} else if (node instanceof AbstractFixtureNode) {
				tile.addFixture(((AbstractFixtureNode<? extends TileFixture>) node)
						.produce(players, warner));
			} else {
				warner.warn(new UnwantedChildException("tile", node.toString(), // NOPMD
						getLine()));
			}
		}
		tile.addFixture(new TextFixture(sbuild.toString().trim(), -1));
		return tile;
	}

	/**
	 * Check whether we contain invalid data. A Tile is valid if it has row,
	 * column, and kind properties and contains only valid fixtures (units,
	 * fortresses, rivers, events, etc.). For forward compatibility, we do not
	 * object to properties we ignore. (But TODO: should we object to "event"
	 * tags, since those *used* to be valid?)
	 * 
	 * 
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if contain invalid data.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		demandProperty("tile", ROW_PROPERTY, warner, false, false);
		demandProperty("tile", COL_PROPERTY, warner, false, false);
		handleDeprecatedProperty("tile", TERRAIN_PROPERTY, OLD_KIND_PROPERTY,
				warner, true, false);
		for (final AbstractXMLNode node : this) {
			if (node instanceof AbstractFixtureNode // ESCA-JAVA0049:
					|| node instanceof RiverNode) {
				node.checkNode(warner, idFactory);
			} else {
				throw new UnwantedChildException("tile", node.toString(),
						getLine());
			}
		}
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, ROW_PROPERTY, COL_PROPERTY,
				TERRAIN_PROPERTY, OLD_KIND_PROPERTY);
	}

	/**
	 * The text associated with the tile.
	 */
	private final StringBuilder sbuild = new StringBuilder("");

	/**
	 * Add text to the tile.
	 * 
	 * @param text the text to add
	 */
	@Override
	public void addText(final String text) {
		sbuild.append(text);
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TileNode";
	}

}

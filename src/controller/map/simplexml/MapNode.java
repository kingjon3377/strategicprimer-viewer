package controller.map.simplexml;

import model.viewer.SPMap;

import org.apache.commons.lang.NotImplementedException;

/**
 * A node generated from the <map> tag.
 * @author Jonathan Lovelace
 *
 */
public class MapNode extends AbstractChildNode<SPMap> {
	/**
	 * Check the node. 
	 * @throws SPFormatException if all is not well.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		throw new NotImplementedException("Placeholder");
	}
	/**
	 * @return the map the XML represented
	 * @throws SPFormatException if something's wrong with the format.
	 */
	@Override
	public SPMap produce() throws SPFormatException {
		throw new NotImplementedException("Placeholder");
	}

}

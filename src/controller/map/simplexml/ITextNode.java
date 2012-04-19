package controller.map.simplexml;
/**
 * An interface for Nodes that have CDATA in the XML attached to them.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public interface ITextNode {
	/**
	 * Add text to the node.
	 * @param text the text to add
	 */
	void addText(final String text);
}

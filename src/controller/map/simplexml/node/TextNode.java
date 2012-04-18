package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.TextFixture;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.simplexml.ITextNode;
/**
 * A Node to produce a TextFixture.
 * @author Jonathan Lovelace
 *
 */
public class TextNode extends AbstractFixtureNode<TextFixture> implements ITextNode {
	/**
	 * Constructor.
	 */
	public TextNode() {
		super(TextFixture.class);
	}
	/**
	 * Produce the equivalent Fixture.
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the TextFixture this represents
	 * @throws SPFormatException never
	 */
	@Override
	public TextFixture produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new TextFixture(sbuild.toString().trim(),
				hasProperty("turn") ? Integer.parseInt(getProperty("turn"))
						: -1);
	}
	/**
	 * The text the fixture encapsulates.
	 */
	private final StringBuilder sbuild = new StringBuilder("");

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return "turn".equals(property);
	}
	/**
	 * Add text to the fixture.
	 * 
	 * @param text
	 *            the text to add
	 */
	@Override
	public void addText(final String text) {
		sbuild.append(text);
	}
	/**
	 * Check whether we contain invalid data. A TextNode is valid if it has no child nodes.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException on invalid data
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("text", iterator().next()
					.toString(), getLine());
		}
	}
	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "TextNode";
	}
}

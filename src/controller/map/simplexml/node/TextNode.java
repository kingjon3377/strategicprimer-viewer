package controller.map.simplexml.node;

import controller.map.SPFormatException;
import controller.map.simplexml.ITextNode;
import model.map.PlayerCollection;
import model.map.fixtures.TextFixture;
/**
 * A Node to produce a TextFixture.
 * @author Jonathan Lovelace
 *
 */
public class TextNode extends AbstractFixtureNode<TextFixture> implements ITextNode {
	/**
	 * Produce the equivalent Fixture.
	 * @param players ignored
	 * @return the TextFixture this represents
	 * @throws SPFormatException never
	 */
	@Override
	public TextFixture produce(final PlayerCollection players) throws SPFormatException {
		return new TextFixture(sbuild.toString().trim(),
				hasProperty("turn") ? Integer.parseInt(getProperty("turn"))
						: -1);
	}
	/**
	 * The text the fixture encapsulates.
	 */
	private final StringBuilder sbuild = new StringBuilder("");

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
	 * @throws SPFormatException on invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Text element shouldn't have child elements", getLine());
		}
	}
}

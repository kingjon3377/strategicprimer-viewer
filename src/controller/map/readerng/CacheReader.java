package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.resources.CacheFixture;
import util.Pair;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for CacheFixtures.
 *
 * @author Jonathan Lovelace
 *
 */
public class CacheReader implements INodeHandler<CacheFixture> {
	/**
	 * Parse a cache.
	 *
	 * @param element the element to read from
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the cache represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public CacheFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(element.getName(), stream);
		final CacheFixture fix = new CacheFixture(
				getAttribute(element, "kind"),
				getAttribute(element, "contents"), getOrGenerateID(element,
						warner, idFactory), XMLHelper.getFile(stream));
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList("cache");
	}

	/** @return the class we now ow to write */
	@Override
	public Class<CacheFixture> writes() {
		return CacheFixture.class;
	}

	/**
	 * Create an intermediate representation to write to a Writer.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the
	 *        adapter work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S extends CacheFixture> SPIntermediateRepresentation write(
			final S obj) {
		return new SPIntermediateRepresentation("cache", Pair.of("kind",
				obj.getKind()), Pair.of("contents", obj.getContents()),
				Pair.of("id", Long.toString(obj.getID())));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CacheReader";
	}
}

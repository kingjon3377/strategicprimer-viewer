package controller.map.cxml;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.Nullable;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.ExplorableFixture;
import util.ArraySet;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for Caves and Battlefields.
 * @author Jonathan Lovelace
 *
 */
public class CompactExplorableReader extends AbstractCompactReader<ExplorableFixture> {
	/**
	 * Singleton object.
	 */
	public static final CompactExplorableReader READER = new CompactExplorableReader();
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS;
	static {
		final Set<String> suppTagsTemp = new ArraySet<>();
		suppTagsTemp.add("cave");
		suppTagsTemp.add("battlefield");
		SUPP_TAGS = NullCleaner.assertNotNull(Collections.unmodifiableSet(suppTagsTemp));
	}
	/**
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(@Nullable final String tag) {
		return SUPP_TAGS.contains(tag);
	}

	/**
	 * @param element a tag
	 * @return the value of its 'dc' property.
	 * @throws SPFormatException on SP format problem
	 */
	private static int getDC(final StartElement element)
			throws SPFormatException {
		return parseInt(getParameter(element, "dc"), element.getLocation()
				.getLineNumber());
	}
	/**
	 * @param elem the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed resource
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public ExplorableFixture read(// $codepro.audit.disable cyclomaticComplexity
			final StartElement elem, final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(elem, "battlefield", "cave");
		final int idNum = getOrGenerateID(elem, warner, idFactory);
		final ExplorableFixture retval;
		String tag = elem.getName().getLocalPart();
		if ("battlefield".equalsIgnoreCase(tag)) {
			retval = new Battlefield(getDC(elem), idNum);
		} else if ("cave".equalsIgnoreCase(tag)) {
			retval = new Cave(getDC(elem), idNum);
		} else {
			throw new UnsupportedTagException(NullCleaner.assertNotNull(tag), elem.getLocation().getLineNumber());
		}
		spinUntilEnd(NullCleaner.assertNotNull(elem.getName()), stream);
		retval.setImage(getParameter(elem, "image", ""));
		return retval;
	}
	/**
	 * Write an object to a stream. TODO: Some way of simplifying this?
	 *
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final ExplorableFixture obj, final int indent) throws IOException {
		ostream.append(indent(indent));
		if (obj instanceof Battlefield) {
			ostream.append("<battlefield ");
			ostream.append("dc=\"");
			ostream.append(Integer.toString(((Battlefield) obj).getDC()));
		} else if (obj instanceof Cave) {
			ostream.append("<cave ");
			ostream.append("dc=\"");
			ostream.append(Integer.toString(((Cave) obj).getDC()));
		} else {
			throw new IllegalStateException("Unhandled ExplorableFixture subtype");
		}
		ostream.append("\" id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append('"');
		ostream.append(imageXML(obj));
		ostream.append(" />\n");
	}
}

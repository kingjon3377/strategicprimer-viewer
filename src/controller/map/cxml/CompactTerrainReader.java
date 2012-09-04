package controller.map.cxml;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.TerrainFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import util.ArraySet;
import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for TerrainFixtures.
 * @author Jonathan Lovelace
 *
 */
public final class CompactTerrainReader extends CompactReaderSuperclass implements CompactReader<TerrainFixture> {
	/**
	 * Singleton.
	 */
	private CompactTerrainReader() {
		// Singleton.
	}
	/**
	 * Singleton object.
	 */
	public static final CompactTerrainReader READER = new CompactTerrainReader();
	/**
	 * Enumeration of the types we know how to handle.
	 */
	private static enum TerrainFixtureType {
		/**
		 * Forest.
		 */
		ForestType("forest"),
		/**
		 * Hill.
		 */
		HillType("hill"),
		/**
		 * Mountain.
		 */
		MountainType("mountain"),
		/**
		 * Oasis.
		 */
		OasisType("oasis"),
		/**
		 * Sandbar.
		 */
		SandbarType("sandbar");
		/**
		 * The tag.
		 */
		public final String tag;
		/**
		 * Constructor.
		 * @param tagString The tag.
		 */
		TerrainFixtureType(final String tagString) {
			tag = tagString;
		}
	}
	/**
	 * Mapping from tags to enum-tags.
	 */
	private static final Map<String, TerrainFixtureType> MAP = new HashMap<String, TerrainFixtureType>(
			TerrainFixtureType.values().length);
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS;
	static {
		final Set<String> suppTagsTemp = new ArraySet<String>();
		for (TerrainFixtureType mt : TerrainFixtureType.values()) {
			MAP.put(mt.tag, mt);
			suppTagsTemp.add(mt.tag);
		}
		SUPP_TAGS = Collections.unmodifiableSet(suppTagsTemp);
	}
	/**
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return SUPP_TAGS.contains(tag);
	}
	/**
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public TerrainFixture read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "forest", "hill", "mountain", "oasis", "sandbar");
		// ESCA-JAVA0177:
		final TerrainFixture retval; // NOPMD
		switch (MAP.get(element.getName().getLocalPart())) {
		case ForestType:
			retval = new Forest(getParameter(element, "kind"),
					hasParameter(element, "rows"), getFile(stream));
			break;
		case HillType:
			retval = new Hill(getOrGenerateID(element, warner, idFactory), getFile(stream));
			break;
		case MountainType:
			retval = new Mountain(getFile(stream));
			break;
		case OasisType:
			retval = new Oasis(getOrGenerateID(element, warner, idFactory), getFile(stream));
			break;
		case SandbarType:
			retval = new Sandbar(getOrGenerateID(element, warner, idFactory), getFile(stream));
			break;
		default:
			throw new IllegalArgumentException("Shouldn't get here");
		}
		spinUntilEnd(element.getName(), stream);
		return retval;
	}
	/**
	 * Write an object to a stream.
	 * @param out The stream to write to.
	 * @param obj The object to write.
	 * @param file The file we're writing to.
	 * @param inclusion Whether to change files if a sub-object was read from a different file
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final TerrainFixture obj, final String file,
			final boolean inclusion, final int indent) throws IOException {
		out.append(indent(indent));
		if (obj instanceof Mountain) {
			out.append("<mountain />\n");
			return; // NOPMD Mountains don't yet have IDs.
		} else if (obj instanceof Forest) {
			out.append("<forest kind=\"");
			out.append(((Forest) obj).getKind());
			if (((Forest) obj).isRows()) {
				out.append("\" rows=\"true");
			}
			out.append("\" />\n");
			return; // NOPMD Neither do Forests.
		} else if (obj instanceof Hill) {
			out.append("<hill ");
		} else if (obj instanceof Oasis) {
			out.append("<oasis ");
		} else if (obj instanceof Sandbar) {
			out.append("<sandbar ");
		} else {
			throw new IllegalStateException("Unexpected TerrainFixture type.");
		}
		out.append("id=\"");
		out.append(Integer.toString(obj.getID()));
		out.append("\" />\n");
	}
}

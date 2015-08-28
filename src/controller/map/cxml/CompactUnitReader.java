package controller.map.cxml;

import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import model.map.IFixture;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for tiles, including rivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class CompactUnitReader extends AbstractCompactReader<Unit> {
	/**
	 * The tag used for a unit.
	 */
	private static final String UNIT_TAG = "unit";

	/**
	 * List of readers we'll try subtags on.
	 */
	private final List<CompactReader<? extends IFixture>> readers;

	/**
	 * Singleton object.
	 */
	public static final CompactUnitReader READER = new CompactUnitReader();

	/**
	 * Singleton.
	 */
	private CompactUnitReader() {
		final List<CompactReader<@NonNull ? extends IFixture>> temp = new ArrayList<>();
		temp.add(CompactMobileReader.READER);
		temp.add(CompactResourceReader.READER);
		temp.add(CompactTerrainReader.READER);
		temp.add(CompactTextReader.READER);
		temp.add(CompactTownReader.READER);
		temp.add(CompactWorkerReader.READER);
		readers = unmodifiableList(temp);
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
	public Unit read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, UNIT_TAG);
		requireNonEmptyParameter(element, "name", false, warner);
		requireNonEmptyParameter(element, "owner", false, warner);
		final Unit retval = new Unit(
						players.getPlayer(
								parseInt(ensureNumeric(getParameter(element,
										"owner", "-1")), element.getLocation()
										.getLineNumber())), parseKind(element,
								warner), getParameter(element, "name", ""),
						getOrGenerateID(element, warner, idFactory));
		retval.setImage(getParameter(element, "image", ""));
		final StringBuilder orders = new StringBuilder();
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				retval.addMember(parseChild(
						NullCleaner.assertNotNull(event.asStartElement()),
						stream, players, idFactory, warner));
			} else if (event.isCharacters()) {
				orders.append(event.asCharacters().getData());
			} else if (event.isEndElement()
					&& element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		retval.setOrders(NullCleaner.assertNotNull(orders.toString().trim()));
		return retval;
	}

	/**
	 * Parse what should be a TileFixture from the XML.
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param idFactory the ID factory to generate IDs with
	 * @param warner the Warning instance to use for warnings
	 * @return the parsed fixture.
	 * @throws SPFormatException on SP format problem
	 */
	private UnitMember parseChild(final StartElement element,
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final IDFactory idFactory,
			final Warning warner) throws SPFormatException {
		final String name = element.getName().getLocalPart();
		assert name != null;
		for (final CompactReader<? extends IFixture> item : readers) {
			if (item.isSupportedTag(name)) {
				final IFixture retval = item.read(element, stream, players,
						warner, idFactory);
				if (retval instanceof UnitMember) {
					return (UnitMember) retval;
				} else {
					throw new UnwantedChildException(UNIT_TAG, name, element
							.getLocation().getLineNumber());
				}
			}
		}
		throw new UnwantedChildException(UNIT_TAG, name, element.getLocation()
				.getLineNumber());
	}

	/**
	 * Parse the kind of unit, from the "kind" or "type" parameter---default the
	 * empty string.
	 *
	 * @param element the current element
	 * @param warner the Warning instance to use
	 * @return the kind of unit
	 * @throws SPFormatException on SP format error.
	 */
	private static String parseKind(final StartElement element,
			final Warning warner) throws SPFormatException {
		try {
			final String retval =
					getParamWithDeprecatedForm(element, "kind", "type", warner);
			if (retval.isEmpty()) {
				warner.warn(new MissingPropertyException(NullCleaner
						.assertNotNull(element.getName().getLocalPart()),
						"kind", element.getLocation().getLineNumber()));
			}
			return retval; // NOPMD
		} catch (final MissingPropertyException except) {
			warner.warn(except);
			return ""; // NOPMD
		}
	}

	/**
	 * @param string a string which should be numeric or empty
	 * @return it, or "-1" if it's empty.
	 */
	private static String ensureNumeric(final String string) {
		if (string.isEmpty()) {
			return "-1"; // NOPMD
		} else {
			return string;
		}
	}

	/**
	 * @param tag a tag
	 * @return whether it's one we can read
	 */
	@Override
	public boolean isSupportedTag(@Nullable final String tag) {
		return UNIT_TAG.equalsIgnoreCase(tag);
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void
			write(final Appendable ostream, final Unit obj, final int indent)
					throws IOException {
		ostream.append(indent(indent));
		ostream.append("<unit owner=\"");
		ostream.append(Integer.toString(obj.getOwner().getPlayerId()));
		if (!obj.getKind().isEmpty()) {
			ostream.append("\" kind=\"");
			ostream.append(obj.getKind());
		}
		if (!obj.getName().isEmpty()) {
			ostream.append("\" name=\"");
			ostream.append(obj.getName());
		}
		ostream.append("\" id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append('"');
		ostream.append(imageXML(obj));
		if (obj.iterator().hasNext() || !obj.getOrders().trim().isEmpty()) {
			ostream.append('>').append(obj.getOrders().trim()).append('\n');
			for (final UnitMember member : obj) {
				CompactReaderAdapter.write(ostream, member, indent + 1);
			}
			ostream.append(indent(indent));
			ostream.append("</unit>\n");
		} else {
			ostream.append(" />\n");
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CompactUnitReader";
	}
}

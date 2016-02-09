package controller.map.cxml;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IFixture;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import org.eclipse.jdt.annotation.NonNull;
import util.EqualsAny;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

import static java.util.Collections.unmodifiableList;

/**
 * A reader for tiles, including rivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
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
	public static final CompactReader<Unit> READER = new CompactUnitReader();

	/**
	 * Singleton.
	 */
	private CompactUnitReader() {
		final List<@NonNull CompactReader<@NonNull ? extends IFixture>> temp =
				new ArrayList<>();
		temp.add(CompactMobileReader.READER);
		temp.add(CompactResourceReader.READER);
		temp.add(CompactTerrainReader.READER);
		temp.add(CompactTextReader.READER);
		temp.add(CompactTownReader.READER);
		temp.add(CompactWorkerReader.READER);
		readers = NullCleaner.assertNotNull(unmodifiableList(temp));
	}

	/**
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
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
													getIntegerParameter(element, "owner",
															-1)), parseKind(element,
				warner), getParameter(element, "name", ""),
											getOrGenerateID(element, warner, idFactory));
		retval.setImage(getParameter(element, "image", ""));
		final StringBuilder orders = new StringBuilder(512);
		for (final XMLEvent event : stream) {
			if (event.isStartElement() && EqualsAny.equalsAny(
					event.asStartElement().getName().getNamespaceURI(),
					ISPReader.NAMESPACE, XMLConstants.NULL_NS_URI)) {
				retval.addMember(parseChild(
						NullCleaner.assertNotNull(event.asStartElement()),
						stream, players, idFactory, warner));
			} else if (event.isCharacters()) {
				orders.append(event.asCharacters().getData());
			} else if (event.isEndElement() &&
					           element.getName().equals(event.asEndElement().getName())) {
				break;
			}
		}
		retval.setOrders(NullCleaner.assertNotNull(orders.toString().trim()));
		return retval;
	}

	/**
	 * Parse what should be a TileFixture from the XML.
	 *
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param idFactory the ID factory to generate IDs with
	 * @param warner    the Warning instance to use for warnings
	 * @return the parsed fixture.
	 * @throws SPFormatException on SP format problem
	 */
	private UnitMember parseChild(final StartElement element,
								  final IteratorWrapper<XMLEvent> stream,
								  final IMutablePlayerCollection players,
								  final IDFactory idFactory,
								  final Warning warner) throws SPFormatException {
		final String name = NullCleaner.assertNotNull(element.getName().getLocalPart());
		for (final CompactReader<? extends IFixture> item : readers) {
			if (item.isSupportedTag(name)) {
				final IFixture retval = item.read(element, stream, players,
						warner, idFactory);
				if (retval instanceof UnitMember) {
					return (UnitMember) retval;
				} else {
					throw new UnwantedChildException(new QName(element.getName()
							                                           .getNamespaceURI(),
							                                          UNIT_TAG), element);
				}
			}
		}
		throw new UnwantedChildException(new QName(element.getName().getNamespaceURI(),
				                                          UNIT_TAG), element);
	}

	/**
	 * Parse the kind of unit, from the "kind" or "type" parameter---default the empty
	 * string.
	 *
	 * @param element the current element
	 * @param warner  the Warning instance to use
	 * @return the kind of unit
	 * @throws SPFormatException on SP format error.
	 */
	private static String parseKind(final StartElement element,
									final Warning warner) throws SPFormatException {
		try {
			final String retval =
					getParamWithDeprecatedForm(element, "kind", "type", warner);
			if (retval.isEmpty()) {
				warner.warn(new MissingPropertyException(element, "kind"));
			}
			return retval; // NOPMD
		} catch (final MissingPropertyException except) {
			warner.warn(except);
			return ""; // NOPMD
		}
	}

	/**
	 * @param tag a tag
	 * @return whether it's one we can read
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return UNIT_TAG.equalsIgnoreCase(tag);
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void
	write(final Appendable ostream, final Unit obj, final int indent)
			throws IOException {
		writeTag(ostream, "unit", indent);
		ostream.append(" owner=\"");
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
			indent(ostream, indent);
			ostream.append("</unit>\n");
		} else {
			ostream.append(" />\n");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactUnitReader";
	}
	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof Unit;
	}

}

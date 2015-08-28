package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.MissingChildException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import model.map.IMapView;
import model.map.IMutablePlayerCollection;
import model.map.MapView;
import util.NullCleaner;
import util.Warning;

/**
 * A reader to read map views from XML and turn them into XML. TODO: changesets.
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
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class ViewReader implements INodeHandler<IMapView> {
	/**
	 * The (main) tag we deal with.
	 */
	private static final String TAG = "view";

	/**
	 * A map reader to use.
	 */
	private static final SPMapReader MAP_READER = new SPMapReader();

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<IMapView> writes() {
		return IMapView.class;
	}

	/**
	 * @return a list of the tags this reader understands.
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList(TAG));
	}

	/**
	 * Parse a view from XML.
	 *
	 * @param element the element to start parsing with.
	 * @param stream the XML tags and such
	 * @param players the collection of players, most likely null at this point
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the produced view
	 * @throws SPFormatException on format problems
	 */
	@Override
	public IMapView parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IMutablePlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final StartElement event = getFirstStartElement(stream, element
				.getLocation().getLineNumber());
		requireMapTag(event, element);
		final IMapView view =
				new MapView(MAP_READER.parse(event, stream, players, warner,
						idFactory), XMLHelper.parseInt(
						getAttribute(element, "current_player"),
						NullCleaner.assertNotNull(element.getLocation())),
						XMLHelper
								.parseInt(
										getAttribute(element, "current_turn"),
										NullCleaner.assertNotNull(element
												.getLocation())));
		XMLHelper.spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		return view;
	}

	/**
	 * @param stream a stream of XMLEvents
	 * @param line the line the parent tag is on
	 * @throws SPFormatException if no start element in stream
	 * @return the first start-element in the stream
	 */
	private static StartElement getFirstStartElement(
			final Iterable<XMLEvent> stream, final int line)
			throws SPFormatException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				return NullCleaner.assertNotNull(event.asStartElement());
			}
		}
		throw new MissingChildException("map", line);
	}

	/**
	 * Create an intermediate representation of the view to convert to XML.
	 * TODO: changesets
	 *
	 * @param <S> the type of the object
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends IMapView> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				TAG);
		retval.addIntegerAttribute("current_player",
				obj.getPlayers().getCurrentPlayer().getPlayerId());
		retval.addIntegerAttribute("current_turn", obj.getCurrentTurn());
		retval.addChild(ReaderAdapter.ADAPTER.write(obj.getMap()));
		return retval;
	}

	/**
	 * Assert that the specified tag is a "map" tag.
	 *
	 * @param element the tag to check
	 * @param context the parent tag
	 * @throws SPFormatException if it isn't
	 */
	private static void requireMapTag(final StartElement element,
			final StartElement context) throws SPFormatException {
		if (!"map".equalsIgnoreCase(element.getName().getLocalPart())) {
			throw new UnwantedChildException(NullCleaner.assertNotNull(context
					.getName().getLocalPart()),
					NullCleaner.assertNotNull(element.asStartElement()
							.getName().getLocalPart()), element.getLocation()
							.getLineNumber());
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ViewReader";
	}
}

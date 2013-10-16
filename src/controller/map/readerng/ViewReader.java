package controller.map.readerng;

import static controller.map.readerng.XMLHelper.assertNonNullList;
import static controller.map.readerng.XMLHelper.assertNonNullQName;
import static controller.map.readerng.XMLHelper.getAttribute;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.MapView;
import model.map.PlayerCollection;
import util.Warning;
import controller.map.formatexceptions.MissingChildException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader to read map views from XML and turn them into XML. TODO: changesets.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class ViewReader implements INodeHandler<MapView> {
	/**
	 * The (main) tag we deal with.
	 */
	private static final String TAG = "view";

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<MapView> writes() {
		return MapView.class;
	}

	/**
	 * @return a list of the tags this reader understands.
	 */
	@Override
	public List<String> understands() {
		return assertNonNullList(Collections.singletonList(TAG));
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
	public MapView parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		MapView view = null;
		final StartElement event = getFirstStartElement(stream, element
				.getLocation().getLineNumber());
		requireMapTag(event, element);
		view = new MapView(MAP_READER.parse(event, stream, players, warner,
				idFactory), Integer.parseInt(getAttribute(element,
				"current_player")), Integer.parseInt(getAttribute(element,
				"current_turn")));
		XMLHelper.spinUntilEnd(assertNonNullQName(element.getName()), stream);
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
				return event.asStartElement();
			}
		}
		throw new MissingChildException("map", line);
	}

	/**
	 * Create an intermediate representation of the view to write to a Writer.
	 * TODO: changesets
	 *
	 * @param <S> the type of the object
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends MapView> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				TAG);
		retval.addAttribute(
				"current_player",
				Integer.toString(obj.getPlayers().getCurrentPlayer()
						.getPlayerId()));
		retval.addAttribute("current_turn",
				Integer.toString(obj.getCurrentTurn()));
		retval.addChild(ReaderAdapter.ADAPTER.write(obj.getMap()));
		return retval;
	}

	/**
	 * A map reader to use.
	 */
	private static final SPMapReader MAP_READER = new SPMapReader();

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
			throw new UnwantedChildException(context.getName().getLocalPart(),
					element.asStartElement().getName().getLocalPart(), element
							.getLocation().getLineNumber());
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

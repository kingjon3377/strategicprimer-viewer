package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.MapView;
import model.map.PlayerCollection;
import model.map.SPMap;
import util.Warning;
import controller.map.MissingChildException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A reader to read map views from XML and turn them into XML. TODO: changesets.
 * 
 * @author Jonathan Lovelace
 * 
 */
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
		return Collections.singletonList(TAG);
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
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				if (view == null) {
					requireMapTag(event.asStartElement(), element);
					view = new MapView(
							// NOPMD
							MAP_READER.parse(event.asStartElement(), stream,
									players, warner, idFactory),
							Integer.parseInt(getAttribute(element,
									"current_player")),
							Integer.parseInt(getAttribute(element,
									"current_turn")), XMLHelper.getFile(stream));
				} else {
					throw new UnwantedChildException(element.getName()
							.getLocalPart(), event.asStartElement().getName()
							.getLocalPart(), event.getLocation()
							.getLineNumber());
				}
			} else if (event.isEndElement()
					&& TAG.equalsIgnoreCase(event.asEndElement().getName()
							.getLocalPart())) {
				break;
			}
		}
		if (view == null) {
			throw new MissingChildException(TAG, element.getLocation()
					.getLineNumber());
		}
		return view;
	}

	/**
	 * Create an intermediate representation to write to a Writer. TODO:
	 * changesets
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
		final SPMapReader reader = MAP_READER;
		final Map<String, SPIntermediateRepresentation> tagMap = new HashMap<String, SPIntermediateRepresentation>();
		tagMap.put(obj.getFile(), retval);
		addChild(tagMap, obj.getMap(), retval, reader);
		return retval;
	}

	/**
	 * Add a child node to a node---the parent node, or an 'include' node
	 * representing its chosen file.
	 * 
	 * @param map the mapping from filenames to IRs.
	 * @param obj the object we're handling
	 * @param parent the parent node, so we can add any include nodes created to
	 *        it
	 * @param reader the reader to use to handle the object
	 */
	private static void addChild(
			final Map<String, SPIntermediateRepresentation> map,
			final SPMap obj, final SPIntermediateRepresentation parent,
			final SPMapReader reader) {
		if (obj.getFile() == null) {
			parent.addChild(reader.write(obj));
		} else {
			if (!map.containsKey(obj.getFile())) {
				final SPIntermediateRepresentation includeTag = new SPIntermediateRepresentation(
						"include");
				includeTag.addAttribute("file", obj.getFile());
				parent.addChild(includeTag);
				map.put(obj.getFile(), includeTag);
			}
			map.get(obj.getFile()).addChild(reader.write(obj));
		}
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
}

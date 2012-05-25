package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.MapView;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMap;
import util.Pair;
import util.Warning;
import controller.map.MissingChildException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
/**
 * A reader to read map views from XML and turn them into XML. TODO: submaps, changesets.
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
	 * TODO: Once we add support for submaps, this reader should be the one to
	 * "understand" (and abort on) XML with <submap> as the root.
	 * 
	 * @return a list of the tags this reader understands.
	 */
	@Override
	public List<String> understands() {
		return Collections.singletonList(TAG);
	}
	/**
	 * Parse a view from XML.
	 * @param element the element to start parsing with.
	 * @param stream the XML tags and such
	 * @param players the collection of players, most likely null at this point
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @return the produced view
	 * @throws SPFormatException on format problems
	 */
	@Override
	public MapView parse(final StartElement element, final Iterable<XMLEvent> stream,
			final PlayerCollection players, final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		MapView view = null;
		for (XMLEvent event : stream) {
			if (event.isStartElement()) {
				if ("map".equalsIgnoreCase(event.asStartElement().getName().getLocalPart()) && view == null) {
					view = new MapView(// NOPMD
							new SPMapReader().parse(event.asStartElement(),
									stream, players, warner, idFactory),
							Integer.parseInt(getAttribute(element,
									"current_player")),
							Integer.parseInt(getAttribute(element,
									"current_turn")),
							XMLHelper.getFile(stream));
				} else if ("submap".equalsIgnoreCase(event.asStartElement()
						.getName().getLocalPart())
						&& view != null) {
					view.addSubmap(
							PointFactory.point(Integer.parseInt(getAttribute(//NOPMD
									event.asStartElement(), "row")), Integer
									.parseInt(getAttribute(
											event.asStartElement(), "column"))),
							parseSubmap(stream, players, warner, idFactory,
									event.asStartElement()));
				} else {
					throw new UnwantedChildException(element.getName()
							.getLocalPart(), event.asStartElement().getName()
							.getLocalPart(), event.getLocation()
							.getLineNumber());
				}
			} else if (event.isEndElement() && TAG.equalsIgnoreCase(event.asEndElement().getName().getLocalPart())) {
				break;
			}
		}
		if (view == null) {
			throw new MissingChildException(TAG, element.getLocation().getLineNumber());
		}
		return view;
	}
	
	/**
	 * Parse a submap. We've already parsed the 'submap' tag itself to get the
	 * coordinates of the tile the submap represents.
	 * 
	 * @param stream
	 *            the stream of tags
	 * @param players
	 *            the collection of players to use
	 * @param warner
	 *            the Warning instance to use
	 * @param idFactory
	 *            the ID factory to use
	 * @param parent
	 *            the parent ('submap') tag, needed for its location on error and to spin-until-end on.
	 * @return the submap
	 * @throws SPFormatException on SP format problems
	 */
	private static SPMap parseSubmap(final Iterable<XMLEvent> stream,
			final PlayerCollection players, final Warning warner,
			final IDFactory idFactory, final StartElement parent)
			throws SPFormatException {
		StartElement element = null;
		for (XMLEvent event : stream) {
			if (event.isStartElement()) {
				element = event.asStartElement();
				break;
			}
		}
		if (element == null) {
			throw new MissingChildException("submap", parent.getLocation().getLineNumber());
		}
		// ESCA-JAVA0177:
		final SPMap retval; // NOPMD
		if ("map".equalsIgnoreCase(element.getName().getLocalPart())) {
			retval = new SPMapReader().parse(element, stream, players, warner, idFactory);
		} else {
			throw new UnwantedChildException("submap", element.getName()
					.getLocalPart(), element.getLocation().getLineNumber());
		}
		XMLHelper.spinUntilEnd(parent.getName(), stream);
		return retval;
	}

	/**
	 * Create an intermediate representation to write to a Writer. TODO: submaps, changesets
	 * @param <S> the type of the object
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends MapView> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(TAG);
		retval.addAttribute(
				"current_player",
				Integer.toString(obj.getPlayers().getCurrentPlayer()
						.getPlayerId()));
		retval.addAttribute("current_turn", Integer.toString(obj.getCurrentTurn()));
		final SPMapReader reader = new SPMapReader();
		final Map<String, SPIntermediateRepresentation> tagMap = new HashMap<String, SPIntermediateRepresentation>();
		tagMap.put(obj.getFile(), retval);
		addChild(tagMap, obj.getMap(), retval, reader);
		for (Entry<Point, SPMap> submap : obj.getSubmapIterator()) {
			tagMap.clear();
			@SuppressWarnings("unchecked")
			final SPIntermediateRepresentation child = new SPIntermediateRepresentation(//NOPMD
					"submap", Pair.of("row",
							Integer.toString(submap.getKey().row())), Pair.of(
							"column", Integer.toString(submap.getKey().col())));
			tagMap.put(obj.getFile(), child);
			addChild(tagMap, submap.getValue(), child, reader);
			retval.addChild(child);
		}
		return retval;
	}
	/**
	 * Add a child node to a node---the parent node, or an 'include' node representing its chosen file.
	 * @param map the mapping from filenames to IRs.
	 * @param obj the object we're handling
	 * @param parent the parent node, so we can add any include nodes created to it
	 * @param reader the reader to use to handle the object
	 */
	private static void addChild(final Map<String, SPIntermediateRepresentation> map,
			final SPMap obj, final SPIntermediateRepresentation parent, final SPMapReader reader) {
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
}

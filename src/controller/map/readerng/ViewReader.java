package controller.map.readerng;

import static controller.map.readerng.XMLHelper.getAttribute;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.MapView;
import model.map.PlayerCollection;
import util.Warning;
import controller.map.MissingChildException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;
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
					view = new MapView(new SPMapReader().parse(//NOPMD
							event.asStartElement(), stream, players, warner,
							idFactory), Integer.parseInt(getAttribute(element,
							"current_player")), Integer.parseInt(getAttribute(
							element, "current_turn")));
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
		if (stream.iterator() instanceof IncludingIterator) {
			view.setFile(((IncludingIterator) stream.iterator()).getFile());
		}
		return view;
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
		final ReaderAdapter adapter = new ReaderAdapter();
		retval.addAttribute(
				"current_player",
				Integer.toString(obj.getPlayers().getCurrentPlayer()
						.getId()));
		retval.addAttribute("current_turn", Integer.toString(obj.getCurrentTurn()));
		retval.addChild(adapter.write(obj.getMap()));
		return retval;
	}
}

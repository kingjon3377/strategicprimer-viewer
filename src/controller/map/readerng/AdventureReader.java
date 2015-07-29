package controller.map.readerng;

import static controller.map.readerng.XMLHelper.spinUntilEnd;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.Player;
import model.map.fixtures.explorable.AdventureFixture;
import util.NullCleaner;
import util.Warning;
/**
 * A reader for adventure hooks.
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class AdventureReader implements INodeHandler<AdventureFixture> {
	/**
	 * Parse an adventure hook.
	 * @param element the element to read from
	 * @param stream a stream of more elements
	 * @param players the list of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the parsed adventure hook
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public AdventureFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final IMutablePlayerCollection players,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		Player player = players.getIndependent();
		if (XMLHelper.hasAttribute(element, "owner")) {
			player =
					players.getPlayer(XMLHelper.parseInt(
							XMLHelper.getAttribute(element, "owner"),
							NullCleaner.assertNotNull(element.getLocation())));
		}
		final AdventureFixture retval =
				new AdventureFixture(player, XMLHelper.getAttribute(element,
						"brief", ""), XMLHelper.getAttribute(element, "full",
						""), XMLHelper.getOrGenerateID(element, warner,
						idFactory));
		XMLHelper.addImage(element, retval);
		return retval;
	}


	/** @return the class we know how to write */
	@Override
	public Class<AdventureFixture> writes() {
		return AdventureFixture.class;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("adventure"));
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation
			write(final AdventureFixture obj) {
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation("adventure");
		retval.addIdAttribute(obj.getID());
		if (!obj.getOwner().isIndependent()) {
			retval.addIntegerAttribute("owner", obj.getOwner().getPlayerId());
		}
		if (!obj.getBriefDescription().isEmpty()) {
			retval.addAttribute("brief", obj.getBriefDescription());
		}
		if (!obj.getFullDescription().isEmpty()) {
			retval.addAttribute("full", obj.getFullDescription());
		}
		retval.addImageAttribute(obj);
		return retval;
	}

}

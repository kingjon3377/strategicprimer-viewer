package impl.xmlio.yaxml;

import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.IPlayerCollection;
import common.map.Player;
import common.map.fixtures.explorable.AdventureFixture;
import common.xmlio.Warning;

import lovelace.util.ThrowingConsumer;

/**
 * A reader for adventure hooks.
 */
/* package */ class YAAdventureReader extends YAAbstractReader<AdventureFixture, AdventureFixture> {
	private final IPlayerCollection players;
	public YAAdventureReader(final Warning warner, final IDRegistrar idFactory, final IPlayerCollection players) {
		super(warner, idFactory);
		this.players = players;
	}

	/**
	 * Read an adventure from XML.
	 */
	@Override
	public AdventureFixture read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		requireTag(element, parent, "adventure");
		expectAttributes(element, "owner", "brief", "full", "image", "id");
		final Player player;
		if (hasParameter(element, "owner")) {
			player = players.getPlayer(getIntegerParameter(element, "owner"));
		} else {
			player = players.getIndependent();
		}
		final AdventureFixture retval = new AdventureFixture(player,
			getParameter(element, "brief", ""), getParameter(element, "full", ""),
			getOrGenerateID(element));
		retval.setImage(getParameter(element, "image", ""));
		spinUntilEnd(element.getName(), stream);
		return retval;
	}

	/**
	 * Write an adventure to XML.
	 */
	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final AdventureFixture obj, final int indent)
			throws IOException {
		writeTag(ostream, "adventure", indent);
		writeProperty(ostream, "id", obj.getId());
		if (!obj.getOwner().isIndependent()) {
			writeProperty(ostream, "owner", obj.getOwner().getPlayerId());
		}
		writeNonemptyProperty(ostream, "brief", obj.getBriefDescription());
		writeNonemptyProperty(ostream, "full", obj.getFullDescription());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "adventure".equalsIgnoreCase(tag);
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof AdventureFixture;
	}
}

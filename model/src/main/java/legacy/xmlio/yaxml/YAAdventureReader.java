package legacy.xmlio.yaxml;

import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.idreg.IDRegistrar;
import legacy.map.ILegacyPlayerCollection;
import legacy.map.Player;
import legacy.map.fixtures.explorable.AdventureFixture;
import lovelace.util.ThrowingConsumer;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.nio.file.Path;

/**
 * A reader for adventure hooks.
 */
/* package */ final class YAAdventureReader extends YAAbstractReader<AdventureFixture, AdventureFixture> {
	private final ILegacyPlayerCollection players;

	public YAAdventureReader(final Warning warner, final IDRegistrar idFactory, final ILegacyPlayerCollection players) {
		super(warner, idFactory);
		this.players = players;
	}

	/**
	 * Read an adventure from XML.
	 */
	@Override
	public AdventureFixture read(final StartElement element, final @Nullable Path path, final QName parent,
	                             final Iterable<XMLEvent> stream)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "adventure");
		expectAttributes(element, path, "owner", "brief", "full", "image", "id");
		final Player player;
		if (hasParameter(element, "owner")) {
			player = players.getPlayer(getIntegerParameter(element, path, "owner"));
		} else {
			player = players.getIndependent();
		}
		final AdventureFixture retval = new AdventureFixture(player,
				getParameter(element, "brief", ""), getParameter(element, "full", ""),
				getOrGenerateID(element, path));
		retval.setImage(getParameter(element, "image", ""));
		spinUntilEnd(element.getName(), path, stream);
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
		if (!obj.owner().isIndependent()) {
			writeProperty(ostream, "owner", obj.owner().getPlayerId());
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

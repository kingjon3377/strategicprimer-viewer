package legacy.xmlio.fluidxml;

import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import legacy.idreg.IDRegistrar;
import legacy.map.ILegacyPlayerCollection;
import legacy.map.Player;
import legacy.map.Point;
import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.explorable.AdventureFixture;
import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.explorable.Cave;
import legacy.map.fixtures.explorable.Portal;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.nio.file.Path;

/* package */ class FluidExplorableHandler extends FluidBase {
	public static AdventureFixture readAdventure(final StartElement element, final Path path,
	                                             final QName parent, final Iterable<XMLEvent> stream,
	                                             final ILegacyPlayerCollection players, final Warning warner,
	                                             final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "adventure");
		expectAttributes(element, warner, "owner", "brief", "full", "id", "image");
		final Player player;
		if (hasAttribute(element, "owner")) {
			player = players.getPlayer(getIntegerAttribute(element, "owner"));
		} else {
			player = players.getIndependent();
		}
		final AdventureFixture retval = setImage(new AdventureFixture(player,
				getAttribute(element, "brief", ""),
				getAttribute(element, "full", ""),
				getOrGenerateID(element, warner, path, idFactory)), element, warner);
		spinUntilEnd(element.getName(), stream);
		return retval;
	}

	public static Portal readPortal(final StartElement element, final @Nullable Path path, final QName parent,
									final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
									final Warning warner, final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, parent, "portal");
		expectAttributes(element, warner, "row", "column", "world", "id", "image");
		final Point location = new Point(getIntegerAttribute(element, "row"),
				getIntegerAttribute(element, "column"));
		final Portal retval = setImage(new Portal(
				getAttribute(element, "world"), location,
				getOrGenerateID(element, warner, path, idFactory)), element, warner);
		spinUntilEnd(element.getName(), stream);
		return retval;
	}

	public static Cave readCave(final StartElement element, final @Nullable Path path, final QName parent,
	                            final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                            final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "cave");
		expectAttributes(element, warner, "id", "dc", "image");
		final int idNum = getOrGenerateID(element, warner, path, idFactory);
		final Cave retval = new Cave(getIntegerAttribute(element, "dc"), idNum);
		spinUntilEnd(element.getName(), stream);
		return setImage(retval, element, warner);
	}

	public static Battlefield readBattlefield(final StartElement element, final @Nullable Path path, final QName parent,
											  final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
											  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "battlefield");
		expectAttributes(element, warner, "id", "dc", "image");
		final int idNum = getOrGenerateID(element, warner, path, idFactory);
		final Battlefield retval = new Battlefield(getIntegerAttribute(element, "dc"), idNum);
		spinUntilEnd(element.getName(), stream);
		return setImage(retval, element, warner);
	}

	public static TextFixture readTextFixture(final StartElement element, final @Nullable Path path, final QName parent,
											  final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
											  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "text");
		expectAttributes(element, warner, "turn", "image");
		return setImage(new TextFixture(getTextUntil(element.getName(), stream),
				getIntegerAttribute(element, "turn", -1, warner)), element, warner);
	}

	public static void writeAdventure(final XMLStreamWriter ostream, final AdventureFixture obj,
									  final int indent) throws XMLStreamException {
		writeTag(ostream, "adventure", indent, true);
		writeAttributes(ostream, Pair.with("id", obj.getId()));
		if (!obj.owner().isIndependent()) {
			writeAttributes(ostream, Pair.with("owner", obj.owner().getPlayerId()));
		}
		writeNonEmptyAttributes(ostream, Pair.with("brief", obj.getBriefDescription()),
				Pair.with("full", obj.getFullDescription()));
		writeImage(ostream, obj);
	}

	public static void writePortal(final XMLStreamWriter ostream, final Portal obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "portal", indent, true);
		writeAttributes(ostream, Pair.with("world", obj.getDestinationWorld()),
				Pair.with("row", obj.getDestinationCoordinates().row()),
				Pair.with("column", obj.getDestinationCoordinates().column()),
				Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static void writeCave(final XMLStreamWriter ostream, final Cave obj, final int indent)
			throws XMLStreamException {
		writeTag(ostream, "cave", indent, true);
		writeAttributes(ostream, Pair.with("dc", obj.getDC()), Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static void writeBattlefield(final XMLStreamWriter ostream, final Battlefield obj,
										final int indent) throws XMLStreamException {
		writeTag(ostream, "battlefield", indent, true);
		writeAttributes(ostream, Pair.with("dc", obj.getDC()), Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
	}

	public static void writeTextFixture(final XMLStreamWriter ostream, final TextFixture obj,
										final int indent) throws XMLStreamException {
		writeTag(ostream, "text", indent, false);
		if (obj.getTurn() != -1) {
			writeAttributes(ostream, Pair.with("turn", obj.getTurn()));
		}
		writeImage(ostream, obj);
		ostream.writeCharacters(obj.getText().strip());
		ostream.writeEndElement();
	}
}

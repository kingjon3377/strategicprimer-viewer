import controller.map.fluidxml {
    XMLHelper
}
import controller.map.misc {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
    IllegalArgumentException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamWriter
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import model.map {
    IPlayerCollection,
    Player,
    PointFactory,
    Point
}
import model.map.fixtures {
    TextFixture
}
import model.map.fixtures.explorable {
    AdventureFixture,
    Portal,
    Cave,
    Battlefield
}

import util {
    Warning
}
AdventureFixture readAdventure(StartElement element, QName parent,
        JIterable<XMLEvent> stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "adventure");
    Player player;
    if (XMLHelper.hasAttribute(element, "owner")) {
        player = players.getPlayer(XMLHelper.getIntegerAttribute(element, "owner"));
    } else {
        player = players.independent;
    }
    AdventureFixture retval = XMLHelper.setImage(AdventureFixture(player,
        XMLHelper.getAttribute(element, "brief", ""),
        XMLHelper.getAttribute(element, "full", ""),
        XMLHelper.getOrGenerateID(element, warner, idFactory)), element, warner);
    XMLHelper.spinUntilEnd(element.name, stream);
    return retval;
}

Portal readPortal(StartElement element, QName parent,
        JIterable<XMLEvent> stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "portal");
    Point location = PointFactory.point(XMLHelper.getIntegerAttribute(element, "row"),
        XMLHelper.getIntegerAttribute(element, "column"));
    Portal retval = XMLHelper.setImage(Portal(
        XMLHelper.getAttribute(element, "world"), location,
        XMLHelper.getOrGenerateID(element, warner, idFactory)),
        element, warner);
    XMLHelper.spinUntilEnd(element.name, stream);
    return retval;
}

Cave readCave(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "cave");
    Integer idNum = XMLHelper.getOrGenerateID(element, warner, idFactory);
    Cave retval = Cave(XMLHelper.getIntegerAttribute(element, "dc"), idNum);
    XMLHelper.spinUntilEnd(element.name, stream);
    return XMLHelper.setImage(retval, element, warner);
}

Battlefield readBattlefield(StartElement element, QName parent,
        JIterable<XMLEvent> stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "battlefield");
    Integer idNum = XMLHelper.getOrGenerateID(element, warner, idFactory);
    Battlefield retval = Battlefield(XMLHelper.getIntegerAttribute(element, "dc"), idNum);
    XMLHelper.spinUntilEnd(element.name, stream);
    return XMLHelper.setImage(retval, element, warner);
}

TextFixture readTextFixture(StartElement element, QName parent,
        JIterable<XMLEvent> stream, IPlayerCollection players, Warning warner,
        IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "text");
    return XMLHelper.setImage(TextFixture(XMLHelper.getTextUntil(element.name, stream),
        XMLHelper.getIntegerAttribute(element, "turn", -1)),
        element, warner);
}

void writeAdventure(XMLStreamWriter ostream, Object obj, Integer indent) {
    // TODO: Create helper method for this idiom, to allow us to condense these methods
    if (is AdventureFixture obj) {
        XMLHelper.writeTag(ostream, "adventure", indent, true);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        if (!obj.owner.independent) {
            XMLHelper.writeIntegerAttribute(ostream, "owner", obj.owner.playerId);
        }
        XMLHelper.writeNonEmptyAttribute(ostream, "brief", obj.briefDescription);
        XMLHelper.writeNonEmptyAttribute(ostream, "full", obj.fullDescription);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write AdventureFixtures");
    }
}

void writePortal(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Portal obj) {
        XMLHelper.writeTag(ostream, "portal", indent, true);
        XMLHelper.writeAttribute(ostream, "world", obj.destinationWorld);
        XMLHelper.writeIntegerAttribute(ostream, "row", obj.destinationCoordinates.row);
        XMLHelper.writeIntegerAttribute(ostream, "column", obj.destinationCoordinates.col);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Portals");
    }
}

void writeCave(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Cave obj) {
        XMLHelper.writeTag(ostream, "cave", indent, true);
        XMLHelper.writeIntegerAttribute(ostream, "dc", obj.dc);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Caves");
    }
}

void writeBattlefield(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Battlefield obj) {
        XMLHelper.writeTag(ostream, "battlefield", indent, true);
        XMLHelper.writeIntegerAttribute(ostream, "dc", obj.dc);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Battlefields");
    }
}

void writeTextFixture(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is TextFixture obj) {
        XMLHelper.writeTag(ostream, "text", indent, false);
        if (obj.turn != -1) {
            XMLHelper.writeIntegerAttribute(ostream, "turn", obj.turn);
        }
        XMLHelper.writeImage(ostream, obj);
        ostream.writeCharacters(obj.text.trimmed);
        ostream.writeEndElement();
    } else {
        throw IllegalArgumentException("Can only write TextFixtures");
    }
}
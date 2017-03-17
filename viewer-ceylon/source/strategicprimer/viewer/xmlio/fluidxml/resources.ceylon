import controller.map.fluidxml {
    XMLHelper
}
import controller.map.formatexceptions {
    MissingPropertyException,
    DeprecatedPropertyException
}
import controller.map.misc {
    IDRegistrar
}

import java.lang {
    JIterable=Iterable,
    JNumber=Number,
    JInteger=Integer,
    IllegalArgumentException
}
import java.math {
    BigDecimal
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
    IPlayerCollection
}
import model.map.fixtures {
    ResourcePile
}
import model.map.fixtures.resources {
    CacheFixture,
    Grove,
    Meadow,
    FieldStatus,
    Mine,
    MineralVein,
    Shrub,
    StoneDeposit,
    StoneKind
}
import model.map.fixtures.towns {
    TownStatus
}

import util {
    Warning,
    Quantity
}
ResourcePile readResource(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "resource");
    XMLHelper.spinUntilEnd(element.name, stream);
    String quantityStr = XMLHelper.getAttribute(element, "quantity");
    JNumber quantity;
    if (quantityStr.contains(".")) {
        quantity = BigDecimal(quantityStr);
    } else {
        value temp = Integer.parse(quantityStr);
        if (is Integer temp) {
            quantity = JInteger(temp);
        } else {
            throw MissingPropertyException(element, "quantity", temp);
        }
    }
    ResourcePile retval = ResourcePile(
        XMLHelper.getOrGenerateID(element, warner, idFactory),
        XMLHelper.getAttribute(element, "kind"),
        XMLHelper.getAttribute(element, "contents"),
        Quantity(quantity, XMLHelper.getAttribute(element, "unit", "")));
    if (XMLHelper.hasAttribute(element, "created")) {
        retval.created = XMLHelper.getIntegerAttribute(element, "created");
    }
    return XMLHelper.setImage(retval, element, warner);
}

CacheFixture readCache(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "cache");
    XMLHelper.spinUntilEnd(element.name, stream);
    return XMLHelper.setImage(
        CacheFixture(XMLHelper.getAttribute(element, "kind"),
            XMLHelper.getAttribute(element, "contents"),
            XMLHelper.getOrGenerateID(element, warner, idFactory)),
        element, warner);
}

Grove readGrove(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "grove");
    XMLHelper.spinUntilEnd(element.name, stream);
    Boolean cultivated;
    if (XMLHelper.hasAttribute(element, "cultivated"),
            is Boolean temp = Boolean.parse(XMLHelper.getAttribute(element, "cultivated"))) {
        cultivated = temp;
    } else if (XMLHelper.hasAttribute(element, "wild"),
            is Boolean temp = Boolean.parse(XMLHelper.getAttribute(element, "wild"))) {
        warner.warn(DeprecatedPropertyException(element, "wild", "cultivated"));
        cultivated = !temp;
    } else {
        throw MissingPropertyException(element, "cultivated");
    }
    return XMLHelper.setImage(
        Grove(false, cultivated,
            XMLHelper.getAttrWithDeprecatedForm(element, "kind", "tree", warner),
            XMLHelper.getOrGenerateID(element, warner, idFactory)),
        element, warner);
}

Grove readOrchard(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "orchard");
    XMLHelper.spinUntilEnd(element.name, stream);
    Boolean cultivated;
    if (XMLHelper.hasAttribute(element, "cultivated"),
        is Boolean temp = Boolean.parse(XMLHelper.getAttribute(element, "cultivated"))) {
        cultivated = temp;
    } else if (XMLHelper.hasAttribute(element, "wild"),
        is Boolean temp = Boolean.parse(XMLHelper.getAttribute(element, "wild"))) {
        warner.warn(DeprecatedPropertyException(element, "wild", "cultivated"));
        cultivated = !temp;
    } else {
        throw MissingPropertyException(element, "cultivated");
    }
    return XMLHelper.setImage(
        Grove(true, cultivated,
            XMLHelper.getAttrWithDeprecatedForm(element, "kind", "tree", warner),
            XMLHelper.getOrGenerateID(element, warner, idFactory)),
        element, warner);
}

Meadow readMeadow(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "meadow");
    XMLHelper.spinUntilEnd(element.name, stream);
    Integer id = XMLHelper.getOrGenerateID(element, warner, idFactory);
    if (!XMLHelper.hasAttribute(element, "status")) {
        warner.warn(MissingPropertyException(element, "status"));
    }
    value cultivated = Boolean.parse(XMLHelper.getAttribute(element, "cultivated"));
    if (is Boolean cultivated) {
        return XMLHelper.setImage(
            Meadow(XMLHelper.getAttribute(element, "kind"), false, cultivated,
                id, FieldStatus.parse(
                    XMLHelper.getAttribute(element, "status",
                        FieldStatus.random(id).string))), element, warner);
    } else {
        throw MissingPropertyException(element, "cultivated", cultivated);
    }
}

Meadow readField(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "field");
    XMLHelper.spinUntilEnd(element.name, stream);
    Integer id = XMLHelper.getOrGenerateID(element, warner, idFactory);
    if (!XMLHelper.hasAttribute(element, "status")) {
        warner.warn(MissingPropertyException(element, "status"));
    }
    value cultivated = Boolean.parse(XMLHelper.getAttribute(element, "cultivated"));
    if (is Boolean cultivated) {
        return XMLHelper.setImage(
            Meadow(XMLHelper.getAttribute(element, "kind"), true, cultivated,
                id, FieldStatus.parse(
                    XMLHelper.getAttribute(element, "status",
                        FieldStatus.random(id).string))), element, warner);
    } else {
        throw MissingPropertyException(element, "cultivated", cultivated);
    }
}

Mine readMine(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "mine");
    XMLHelper.spinUntilEnd(element.name, stream);
    return XMLHelper.setImage(
        Mine(
            XMLHelper.getAttrWithDeprecatedForm(element, "kind", "product", warner),
            TownStatus.parseTownStatus(XMLHelper.getAttribute(element, "status")),
            XMLHelper.getOrGenerateID(element, warner, idFactory)), element, warner);
}

MineralVein readMineral(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "mineral");
    XMLHelper.spinUntilEnd(element.name, stream);
    value exposed = Boolean.parse(XMLHelper.getAttribute(element, "exposed"));
    if (is Boolean exposed) {
        return XMLHelper.setImage(
            MineralVein(
                XMLHelper.getAttrWithDeprecatedForm(element, "kind", "mineral", warner),
                exposed, XMLHelper.getIntegerAttribute(element, "dc"),
                XMLHelper.getOrGenerateID(element, warner, idFactory)), element, warner);
    } else {
        throw MissingPropertyException(element, "exposed", exposed);
    }
}

Shrub readShrub(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "shrub");
    XMLHelper.spinUntilEnd(element.name, stream);
    return XMLHelper.setImage(Shrub(
        XMLHelper.getAttrWithDeprecatedForm(element, "kind", "shrub", warner),
        XMLHelper.getOrGenerateID(element, warner, idFactory)),
        element, warner);
}

StoneDeposit readStone(StartElement element, QName parent, JIterable<XMLEvent> stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    XMLHelper.requireTag(element, parent, "stone");
    XMLHelper.spinUntilEnd(element.name, stream);
    return XMLHelper.setImage(
        StoneDeposit(StoneKind.parseStoneKind(
            XMLHelper.getAttrWithDeprecatedForm(element, "kind", "stone", warner)),
            XMLHelper.getIntegerAttribute(element, "dc"),
            XMLHelper.getOrGenerateID(element, warner, idFactory)),
        element, warner);
}

void writeResource(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is ResourcePile obj) {
        XMLHelper.writeTag(ostream, "resource", indent, true);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeAttribute(ostream, "kind", obj.kind);
        XMLHelper.writeAttribute(ostream, "contents", obj.contents);
        switch (quantity = obj.quantity.number)
        case (is JInteger) {
            XMLHelper.writeIntegerAttribute(ostream, "quantity", quantity.intValue());
        }
        case (is BigDecimal) {
            if (quantity.scale() > 0) {
                XMLHelper.writeAttribute(ostream, "quantity", quantity.toPlainString());
            } else {
                XMLHelper.writeIntegerAttribute(ostream, "quantity", quantity.intValue());
            }
        }
        else {
            throw IllegalArgumentException("ResourcePile with non-Integer, non-BigDecimal quantity");
        }
        XMLHelper.writeAttribute(ostream, "unit", obj.quantity.units);
        if (obj.created >= 0) {
            XMLHelper.writeIntegerAttribute(ostream, "created", obj.created);
        }
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write ResourcePiles");
    }
}

void writeCache(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is CacheFixture obj) {
        XMLHelper.writeTag(ostream, "cache", indent, true);
        XMLHelper.writeAttribute(ostream, "kind", obj.kind);
        XMLHelper.writeAttribute(ostream, "contents", obj.contents);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write CacheFixtures");
    }
}

void writeMeadow(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Meadow obj) {
        XMLHelper.writeTag(ostream, (obj.field) then "field" else "meadow", indent, true);
        XMLHelper.writeAttribute(ostream, "kind", obj.kind);
        XMLHelper.writeBooleanAttribute(ostream, "cultivated", obj.cultivated);
        XMLHelper.writeAttribute(ostream, "status", obj.status.string);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Meadows");
    }
}

void writeGrove(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Grove obj) {
        XMLHelper.writeTag(ostream, (obj.orchard) then "orchard" else "grove", indent, true);
        XMLHelper.writeBooleanAttribute(ostream, "cultivated", obj.cultivated);
        XMLHelper.writeAttribute(ostream, "kind", obj.kind);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Groves");
    }
}

void writeMine(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Mine obj) {
        XMLHelper.writeTag(ostream, "mine", indent, true);
        XMLHelper.writeAttribute(ostream, "kind", obj.kind);
        XMLHelper.writeAttribute(ostream, "status", obj.status.string);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Mines");
    }
}

void writeMineral(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is MineralVein obj) {
        XMLHelper.writeTag(ostream, "mineral", indent, true);
        XMLHelper.writeAttribute(ostream, "kind", obj.kind);
        XMLHelper.writeBooleanAttribute(ostream, "exposed", obj.exposed);
        XMLHelper.writeIntegerAttribute(ostream, "dc", obj.dc);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write MineralVeins");
    }
}

void writeStone(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is StoneDeposit obj) {
        XMLHelper.writeTag(ostream, "stone", indent, true);
        XMLHelper.writeAttribute(ostream, "kind", obj.stone().string);
        XMLHelper.writeIntegerAttribute(ostream, "dc", obj.dc);
        XMLHelper.writeIntegerAttribute(ostream, "id", obj.id);
        XMLHelper.writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write StoneDeposits");
    }
}
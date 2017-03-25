import controller.map.formatexceptions {
    MissingPropertyException,
    DeprecatedPropertyException
}
import strategicprimer.viewer.model {
    IDRegistrar
}

import java.lang {
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
import strategicprimer.viewer.model.map.fixtures.resources {
    FieldStatus,
    Grove,
    Meadow,
    CacheFixture,
    Mine,
    StoneDeposit,
    StoneKind,
    Shrub,
    MineralVein
}
import strategicprimer.viewer.model.map.fixtures.towns {
    TownStatus
}

import util {
    Warning,
    Quantity
}
ResourcePile readResource(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "resource");
    spinUntilEnd(element.name, stream);
    String quantityStr = getAttribute(element, "quantity");
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
        getOrGenerateID(element, warner, idFactory),
        getAttribute(element, "kind"),
        getAttribute(element, "contents"),
        Quantity(quantity, getAttribute(element, "unit", "")));
    if (hasAttribute(element, "created")) {
        retval.created = getIntegerAttribute(element, "created");
    }
    return setImage(retval, element, warner);
}

CacheFixture readCache(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "cache");
    spinUntilEnd(element.name, stream);
    return setImage(
        CacheFixture(getAttribute(element, "kind"),
            getAttribute(element, "contents"),
            getOrGenerateID(element, warner, idFactory)),
        element, warner);
}

Grove readGrove(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "grove");
    spinUntilEnd(element.name, stream);
    Boolean cultivated;
    if (hasAttribute(element, "cultivated"),
            is Boolean temp = Boolean.parse(getAttribute(element, "cultivated"))) {
        cultivated = temp;
    } else if (hasAttribute(element, "wild"),
            is Boolean temp = Boolean.parse(getAttribute(element, "wild"))) {
        warner.warn(DeprecatedPropertyException(element, "wild", "cultivated"));
        cultivated = !temp;
    } else {
        throw MissingPropertyException(element, "cultivated");
    }
    return setImage(
        Grove(false, cultivated,
            getAttrWithDeprecatedForm(element, "kind", "tree", warner),
            getOrGenerateID(element, warner, idFactory)),
        element, warner);
}

Grove readOrchard(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "orchard");
    spinUntilEnd(element.name, stream);
    Boolean cultivated;
    if (hasAttribute(element, "cultivated"),
        is Boolean temp = Boolean.parse(getAttribute(element, "cultivated"))) {
        cultivated = temp;
    } else if (hasAttribute(element, "wild"),
        is Boolean temp = Boolean.parse(getAttribute(element, "wild"))) {
        warner.warn(DeprecatedPropertyException(element, "wild", "cultivated"));
        cultivated = !temp;
    } else {
        throw MissingPropertyException(element, "cultivated");
    }
    return setImage(
        Grove(true, cultivated,
            getAttrWithDeprecatedForm(element, "kind", "tree", warner),
            getOrGenerateID(element, warner, idFactory)),
        element, warner);
}

Meadow readMeadow(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "meadow");
    spinUntilEnd(element.name, stream);
    Integer id = getOrGenerateID(element, warner, idFactory);
    if (!hasAttribute(element, "status")) {
        warner.warn(MissingPropertyException(element, "status"));
    }
    value cultivated = Boolean.parse(getAttribute(element, "cultivated"));
    if (is Boolean cultivated) {
        FieldStatus? status = FieldStatus.parse(getAttribute(element, "status",
            FieldStatus.random(id).string));
        if (exists status) {
            return setImage(
                Meadow(getAttribute(element, "kind"), false, cultivated,
                    id, status), element, warner);
        } else {
            throw MissingPropertyException(element, "status");
        }
    } else {
        throw MissingPropertyException(element, "cultivated", cultivated);
    }
}

Meadow readField(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "field");
    spinUntilEnd(element.name, stream);
    Integer id = getOrGenerateID(element, warner, idFactory);
    if (!hasAttribute(element, "status")) {
        warner.warn(MissingPropertyException(element, "status"));
    }
    value cultivated = Boolean.parse(getAttribute(element, "cultivated"));
    if (is Boolean cultivated) {
        FieldStatus? status = FieldStatus.parse(getAttribute(element, "status",
            FieldStatus.random(id).string));
        if (exists status) {
            return setImage(
                Meadow(getAttribute(element, "kind"), true, cultivated,
                    id, status), element, warner);
        } else {
            throw MissingPropertyException(element, "status");
        }
    } else {
        throw MissingPropertyException(element, "cultivated", cultivated);
    }
}

Mine readMine(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "mine");
    spinUntilEnd(element.name, stream);
    if (exists status = TownStatus.parse(getAttribute(element, "status"))) {
        return setImage(
            Mine(getAttrWithDeprecatedForm(element, "kind", "product", warner),
                status, getOrGenerateID(element, warner, idFactory)), element, warner);
    } else {
        throw MissingPropertyException(element, "status");
    }
}

MineralVein readMineral(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "mineral");
    spinUntilEnd(element.name, stream);
    value exposed = Boolean.parse(getAttribute(element, "exposed"));
    if (is Boolean exposed) {
        return setImage(
            MineralVein(
                getAttrWithDeprecatedForm(element, "kind", "mineral", warner),
                exposed, getIntegerAttribute(element, "dc"),
                getOrGenerateID(element, warner, idFactory)), element, warner);
    } else {
        throw MissingPropertyException(element, "exposed", exposed);
    }
}

Shrub readShrub(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "shrub");
    spinUntilEnd(element.name, stream);
    return setImage(Shrub(
        getAttrWithDeprecatedForm(element, "kind", "shrub", warner),
        getOrGenerateID(element, warner, idFactory)),
        element, warner);
}

StoneDeposit readStone(StartElement element, QName parent, {XMLEvent*} stream,
        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
    requireTag(element, parent, "stone");
    spinUntilEnd(element.name, stream);
    if (exists stone = StoneKind.parse(getAttrWithDeprecatedForm(element, "kind", "stone", warner))) {
        return setImage(
            StoneDeposit(stone,
                getIntegerAttribute(element, "dc"),
                getOrGenerateID(element, warner, idFactory)),
            element, warner);
    } else {
        throw MissingPropertyException(element, "stone");
    }
}

void writeResource(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is ResourcePile obj) {
        writeTag(ostream, "resource", indent, true);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeAttribute(ostream, "kind", obj.kind);
        writeAttribute(ostream, "contents", obj.contents);
        switch (quantity = obj.quantity.number)
        case (is JInteger) {
            writeIntegerAttribute(ostream, "quantity", quantity.intValue());
        }
        case (is BigDecimal) {
            if (quantity.scale() > 0) {
                writeAttribute(ostream, "quantity", quantity.toPlainString());
            } else {
                writeIntegerAttribute(ostream, "quantity", quantity.intValue());
            }
        }
        else {
            throw IllegalArgumentException("ResourcePile with non-Integer, non-BigDecimal quantity");
        }
        writeAttribute(ostream, "unit", obj.quantity.units);
        if (obj.created >= 0) {
            writeIntegerAttribute(ostream, "created", obj.created);
        }
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write ResourcePiles");
    }
}

void writeCache(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is CacheFixture obj) {
        writeTag(ostream, "cache", indent, true);
        writeAttribute(ostream, "kind", obj.kind);
        writeAttribute(ostream, "contents", obj.contents);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write CacheFixtures");
    }
}

void writeMeadow(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Meadow obj) {
        writeTag(ostream, (obj.field) then "field" else "meadow", indent, true);
        writeAttribute(ostream, "kind", obj.kind);
        writeBooleanAttribute(ostream, "cultivated", obj.cultivated);
        writeAttribute(ostream, "status", obj.status.string);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Meadows");
    }
}

void writeGrove(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Grove obj) {
        writeTag(ostream, (obj.orchard) then "orchard" else "grove", indent, true);
        writeBooleanAttribute(ostream, "cultivated", obj.cultivated);
        writeAttribute(ostream, "kind", obj.kind);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Groves");
    }
}

void writeMine(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is Mine obj) {
        writeTag(ostream, "mine", indent, true);
        writeAttribute(ostream, "kind", obj.kind);
        writeAttribute(ostream, "status", obj.status.string);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write Mines");
    }
}

void writeMineral(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is MineralVein obj) {
        writeTag(ostream, "mineral", indent, true);
        writeAttribute(ostream, "kind", obj.kind);
        writeBooleanAttribute(ostream, "exposed", obj.exposed);
        writeIntegerAttribute(ostream, "dc", obj.dc);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write MineralVeins");
    }
}

void writeStone(XMLStreamWriter ostream, Object obj, Integer indent) {
    if (is StoneDeposit obj) {
        writeTag(ostream, "stone", indent, true);
        writeAttribute(ostream, "kind", obj.stone.string);
        writeIntegerAttribute(ostream, "dc", obj.dc);
        writeIntegerAttribute(ostream, "id", obj.id);
        writeImage(ostream, obj);
    } else {
        throw IllegalArgumentException("Can only write StoneDeposits");
    }
}
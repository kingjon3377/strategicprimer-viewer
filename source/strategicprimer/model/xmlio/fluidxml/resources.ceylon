import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamWriter
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent,
	EndElement
}

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map {
    IPlayerCollection
}
import strategicprimer.model.map.fixtures {
    ResourcePile,
    Quantity,
    Implement
}
import strategicprimer.model.map.fixtures.resources {
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
import strategicprimer.model.map.fixtures.towns {
    TownStatus
}
import strategicprimer.model.xmlio {
    Warning
}
import strategicprimer.model.xmlio.exceptions {
    MissingPropertyException,
    DeprecatedPropertyException,
	UnwantedChildException
}

object fluidResourceHandler extends FluidBase() {
	shared ResourcePile readResource(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "resource");
	    expectAttributes(element, warner, "quantity", "kind", "contents", "unit", "created",
			"id", "image");
	    spinUntilEnd(element.name, stream);
	    ResourcePile retval = ResourcePile(
	        getOrGenerateID(element, warner, idFactory),
	        getAttribute(element, "kind"),
	        getAttribute(element, "contents"),
	        Quantity(getNumericAttribute(element, "quantity"), getAttribute(element,
				"unit", "")));
	    if (hasAttribute(element, "created")) {
	        retval.created = getIntegerAttribute(element, "created");
	    }
	    return setImage(retval, element, warner);
	}

	shared Implement readImplement(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "implement");
	    expectAttributes(element, warner, "kind", "id", "count", "image");
	    spinUntilEnd(element.name, stream);
	    return setImage(Implement(getAttribute(element, "kind"),
	        getOrGenerateID(element, warner, idFactory),
	        getIntegerAttribute(element, "count", 1, warner)), element, warner);
	}

	shared CacheFixture readCache(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "cache");
	    expectAttributes(element, warner, "kind", "contents", "id", "image");
	    // We want to transition from arbitrary-String 'contents' to sub-tags. As a first
	    // step, future-proof *this* version of the suite by only firing a warning if
	    // such children are detected, instead of aborting.
	    for (event in stream) {
	        if (is StartElement event, isSPStartElement(event)) {
	            if (event.name.localPart.lowercased == "resource" ||
		                event.name.localPart.lowercased == "implement") {
	                warner.handle(UnwantedChildException(element.name, event));
	            } else {
		            throw UnwantedChildException(element.name, event);
		        }
	        } else if (is EndElement event, element.name == event.name) {
	            break;
	        }
	    }
	    return setImage(
	        CacheFixture(getAttribute(element, "kind"),
	            getAttribute(element, "contents"),
	            getOrGenerateID(element, warner, idFactory)),
	        element, warner);
	}

	shared Grove readGrove(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "grove");
	    expectAttributes(element, warner, "cultivated", "wild", "kind", "tree", "id",
			"image", "count");
	    spinUntilEnd(element.name, stream);
	    Boolean cultivated;
	    if (hasAttribute(element, "cultivated")) {
	        cultivated = getBooleanAttribute(element, "cultivated");
	    } else if (hasAttribute(element, "wild")) {
	        warner.handle(DeprecatedPropertyException(element, "wild", "cultivated"));
	        cultivated = !getBooleanAttribute(element, "wild");
	    } else {
	        throw MissingPropertyException(element, "cultivated");
	    }
	    return setImage(
	        Grove(false, cultivated,
	            getAttrWithDeprecatedForm(element, "kind", "tree", warner),
	            getOrGenerateID(element, warner, idFactory), getIntegerAttribute(element,
					"count", -1)),
	        element, warner);
	}

	shared Grove readOrchard(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "orchard");
	    expectAttributes(element, warner, "cultivated", "wild", "kind", "tree", "id",
			"image", "count");
	    spinUntilEnd(element.name, stream);
	    Boolean cultivated;
	    if (hasAttribute(element, "cultivated")) {
	        cultivated = getBooleanAttribute(element, "cultivated");
	    } else if (hasAttribute(element, "wild")) {
	        warner.handle(DeprecatedPropertyException(element, "wild", "cultivated"));
	        cultivated = !getBooleanAttribute(element, "wild");
	    } else {
	        throw MissingPropertyException(element, "cultivated");
	    }
	    return setImage(
	        Grove(true, cultivated,
	            getAttrWithDeprecatedForm(element, "kind", "tree", warner),
	            getOrGenerateID(element, warner, idFactory), getIntegerAttribute(element,
					"count", -1)),
	        element, warner);
	}

	shared Meadow readMeadow(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "meadow");
	    expectAttributes(element, warner, "status", "kind", "cultivated", "id", "image",
			"acres");
	    spinUntilEnd(element.name, stream);
	    Integer id = getOrGenerateID(element, warner, idFactory);
	    if (!hasAttribute(element, "status")) {
	        warner.handle(MissingPropertyException(element, "status"));
	    }
	    value status = FieldStatus.parse(getAttribute(element, "status",
	        FieldStatus.random(id).string));
	    if (is FieldStatus status) {
	        return setImage(Meadow(getAttribute(element, "kind"), false,
	            getBooleanAttribute(element, "cultivated"), id, status,
	            getNumericAttribute(element, "acres", -1)), element, warner);
	    } else {
	        throw MissingPropertyException(element, "status", status);
	    }
	}

	shared Meadow readField(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "field");
	    expectAttributes(element, warner, "status", "kind", "cultivated", "id", "image",
			"acres");
	    spinUntilEnd(element.name, stream);
	    Integer id = getOrGenerateID(element, warner, idFactory);
	    if (!hasAttribute(element, "status")) {
	        warner.handle(MissingPropertyException(element, "status"));
	    }
	    value status = FieldStatus.parse(getAttribute(element, "status",
	        FieldStatus.random(id).string));
	    if (is FieldStatus status) {
	        return setImage(Meadow(getAttribute(element, "kind"), true,
	            getBooleanAttribute(element, "cultivated"), id, status,
	            getNumericAttribute(element, "acres", -1)), element, warner);
	    } else {
	        throw MissingPropertyException(element, "status", status);
	    }
	}

	shared Mine readMine(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "mine");
	    expectAttributes(element, warner, "status", "kind", "product", "id", "image");
	    spinUntilEnd(element.name, stream);
	    value status = TownStatus.parse(getAttribute(element, "status"));
	    if (is TownStatus status) {
	        return setImage(
	            Mine(getAttrWithDeprecatedForm(element, "kind", "product", warner),
	                status, getOrGenerateID(element, warner, idFactory)), element, warner);
	    } else {
	        throw MissingPropertyException(element, "status", status);
	    }
	}

	shared MineralVein readMineral(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "mineral");
	    expectAttributes(element, warner, "kind", "mineral", "exposed", "dc", "id", "image");
	    spinUntilEnd(element.name, stream);
	    return setImage(
	        MineralVein(
	            getAttrWithDeprecatedForm(element, "kind", "mineral", warner),
	            getBooleanAttribute(element, "exposed"), getIntegerAttribute(element, "dc"),
	            getOrGenerateID(element, warner, idFactory)), element, warner);
	}

	shared Shrub readShrub(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "shrub");
	    expectAttributes(element, warner, "kind", "shrub", "id", "image", "count");
	    spinUntilEnd(element.name, stream);
	    return setImage(Shrub(
	        getAttrWithDeprecatedForm(element, "kind", "shrub", warner),
	        getOrGenerateID(element, warner, idFactory), getIntegerAttribute(element,
				"count", -1)),
	        element, warner);
	}

	shared StoneDeposit readStone(StartElement element, QName parent, {XMLEvent*} stream,
	        IPlayerCollection players, Warning warner, IDRegistrar idFactory) {
	    requireTag(element, parent, "stone");
	    expectAttributes(element, warner, "kind", "stone", "dc", "id", "image");
	    spinUntilEnd(element.name, stream);
	    value stone = StoneKind.parse(getAttrWithDeprecatedForm(element, "kind",
	        "stone", warner));
	    if (is StoneKind stone) {
	        return setImage(
	            StoneDeposit(stone,
	                getIntegerAttribute(element, "dc"),
	                getOrGenerateID(element, warner, idFactory)),
	            element, warner);
	    } else {
	        throw MissingPropertyException(element, "kind", stone);
	    }
	}

	shared void writeResource(XMLStreamWriter ostream, ResourcePile obj, Integer indent) {
	    writeTag(ostream, "resource", indent, true);
	    writeAttributes(ostream, "id"->obj.id, "kind"->obj.kind,
	        "contents"->obj.contents, "quantity"->obj.quantity.number,
			"unit"->obj.quantity.units);
	    if (obj.created >= 0) {
	        writeAttributes(ostream, "created"->obj.created);
	    }
	    writeImage(ostream, obj);
	}

	shared void writeImplement(XMLStreamWriter ostream, Implement obj, Integer indent) {
	    writeTag(ostream, "implement", indent, true);
	    writeAttributes(ostream, "kind"->obj.kind, "id"->obj.id);
	    if (obj.count > 1) {
	        writeAttributes(ostream, "count"->obj.count);
	    }
	    writeImage(ostream, obj);
	}
	shared void writeCache(XMLStreamWriter ostream, CacheFixture obj, Integer indent) {
	    writeTag(ostream, "cache", indent, true);
	    writeAttributes(ostream, "kind"->obj.kind, "contents"->obj.contents,
	        "id"->obj.id);
	    writeImage(ostream, obj);
	}

	shared void writeMeadow(XMLStreamWriter ostream, Meadow obj, Integer indent) {
	    writeTag(ostream, (obj.field) then "field" else "meadow", indent, true);
	    writeAttributes(ostream, "kind"->obj.kind, "cultivated"->obj.cultivated,
	        "status"->obj.status.string, "id"->obj.id);
	    if (obj.acres.positive) {
	        writeAttributes(ostream, "acres"->obj.acres);
	    }
	    writeImage(ostream, obj);
	}

	shared void writeGrove(XMLStreamWriter ostream, Grove obj, Integer indent) {
	    writeTag(ostream, (obj.orchard) then "orchard" else "grove", indent, true);
	    writeAttributes(ostream, "cultivated"->obj.cultivated, "kind"->obj.kind,
	        "id"->obj.id);
	    if (obj.population >= 1) {
	        writeAttributes(ostream, "count"->obj.population);
	    }
	    writeImage(ostream, obj);
	}

	shared void writeMine(XMLStreamWriter ostream, Mine obj, Integer indent) {
	    writeTag(ostream, "mine", indent, true);
	    writeAttributes(ostream, "kind"->obj.kind, "status"->obj.status.string,
	        "id"->obj.id);
	    writeImage(ostream, obj);
	}

	shared void writeMineral(XMLStreamWriter ostream, MineralVein obj, Integer indent) {
	    writeTag(ostream, "mineral", indent, true);
	    writeAttributes(ostream, "kind"->obj.kind, "exposed"->obj.exposed, "dc"->obj.dc,
	        "id"->obj.id);
	    writeImage(ostream, obj);
	}

	shared void writeStone(XMLStreamWriter ostream, StoneDeposit obj, Integer indent) {
	    writeTag(ostream, "stone", indent, true);
	    writeAttributes(ostream, "kind"->obj.stone.string, "dc"->obj.dc, "id"->obj.id);
	    writeImage(ostream, obj);
	}

	shared void writeShrub(XMLStreamWriter ostream, Shrub obj, Integer indent) {
		writeTag(ostream, "shrub", indent, true);
		writeAttributes(ostream, "kind"->obj.kind, "id"->obj.id);
		if (obj.population >= 1) {
			writeAttributes(ostream, "count"->obj.population);
		}
		writeImage(ostream, obj);
	}
}
import ceylon.language.meta {
    classDeclaration
}

import strategicprimer.viewer.model {
    IDRegistrar
}

import java.lang {
    JAppendable=Appendable,
    IllegalArgumentException
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent,
    Characters
}
import strategicprimer.viewer.model.map {
    IPlayerCollection,
    HasPortrait
}
import strategicprimer.viewer.model.map.fixtures {
    UnitMember
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit,
    Unit
}

import strategicprimer.viewer.xmlio {
    Warning,
    UnwantedChildException,
    MissingPropertyException
}

"A reader for units."
class YAUnitReader(Warning warner, IDRegistrar idRegistrar, IPlayerCollection players)
        extends YAAbstractReader<IUnit>(warner, idRegistrar) {
    value readers = { YAMobileReader(warner, idRegistrar),
        YAResourceReader(warner, idRegistrar), YATerrainReader(warner, idRegistrar),
        YATextReader(warner, idRegistrar), YAWorkerReader(warner, idRegistrar),
        YAResourcePileReader(warner, idRegistrar), YAImplementReader(warner, idRegistrar)
    };
    """Parse the kind of unit, from the "kind" or deprecated "type" parameter, but merely
       warn if neither is present."""
    String parseKind(StartElement element) {
        try {
            String retval = getParamWithDeprecatedForm(element, "kind", "type");
            if (retval.empty) {
                warner.handle(MissingPropertyException(element, "kind"));
            }
            return retval;
        } catch (MissingPropertyException except) {
            warner.handle(except);
            return "";
        }
    }
    "Parse orders for a unit for a specified turn."
    void parseOrders(StartElement element, IUnit unit, {XMLEvent*} stream) {
        Integer turn = getIntegerParameter(element, "turn", -1);
        StringBuilder builder = StringBuilder();
        for (event in stream) {
            if (is Characters event) {
                builder.append(event.data);
            } else if (is StartElement event) {
                throw UnwantedChildException(element.name, event);
            } else if (isMatchingEnd(element.name, event)) {
                break;
            }
        }
        unit.setOrders(turn, builder.string.trimmed);
    }
    "Parse results for a unit for a specified turn."
    void parseResults(StartElement element, IUnit unit, {XMLEvent*} stream) {
        Integer turn = getIntegerParameter(element, "turn");
        StringBuilder builder = StringBuilder();
        for (event in stream) {
            if (is Characters event) {
                builder.append(event.data);
            } else if (is StartElement event) {
                throw UnwantedChildException(element.name, event);
            } else if (isMatchingEnd(element.name, event)) {
                break;
            }
        }
        unit.setResults(turn, builder.string.trimmed);
    }
    UnitMember parseChild(StartElement element, QName parent, {XMLEvent*} stream) {
        String name = element.name.localPart.lowercased;
        for (reader in readers) {
            if (reader.isSupportedTag(name)) {
                if (is UnitMember retval = reader.read(element, parent, stream)) {
                    return retval;
                } else {
                    throw UnwantedChildException(parent, element);
                }
            }
        } else {
            throw UnwantedChildException(parent, element);
        }
    }
    shared actual IUnit read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "unit");
        requireNonEmptyParameter(element, "name", false);
        requireNonEmptyParameter(element, "owner", false);
        Unit retval = Unit(
            players.getPlayer(getIntegerParameter(element, "owner", -1)),
            parseKind(element), getParameter(element, "name", ""),
            getOrGenerateID(element));
        retval.image = getParameter(element, "image", "");
        retval.portrait = getParameter(element, "portrait", "");
        StringBuilder orders = StringBuilder();
        for (event in stream) {
            if (is StartElement event, isSPStartElement(event)) {
                if ("orders" == event.name.localPart.lowercased) {
                    parseOrders(event, retval, stream);
                } else if ("results" == event.name.localPart.lowercased) {
                    parseResults(event, retval, stream);
                } else {
                    retval.addMember(parseChild(event, element.name, stream));
                }
            } else if (is Characters event) {
                orders.append(event.data);
            } else if (isMatchingEnd(element.name, event)) {
                break;
            }
            String tempOrders = orders.string.trimmed;
            if (!tempOrders.empty) {
                retval.setOrders(-1, tempOrders);
            }
        }
        return retval;
    }
    shared actual Boolean isSupportedTag(String tag) => "unit" == tag.lowercased;
    void writeOrders(JAppendable ostream, String tag, Integer turn, String orders,
            Integer indent) {
        if (orders.empty) {
            return;
        }
        writeTag(ostream, tag, indent);
        if (turn >= 0) {
            writeProperty(ostream, "turn", turn);
        }
        ostream.append('>');
        ostream.append(simpleQuote(orders));
        closeTag(ostream, 0, tag);
    }
    void writeChild(JAppendable ostream, UnitMember child, Integer indent) {
        for (reader in readers) {
            if (reader.canWrite(child)) {
                reader.writeRaw(ostream, child, indent);
                return;
            }
        } else {
            throw IllegalArgumentException("After checking ``readers
                .size`` readers, don't know how to write a ``classDeclaration(child)
                .name``");
        }
    }
    shared actual void write(JAppendable ostream, IUnit obj, Integer indent) {
        writeTag(ostream, "unit", indent);
        writeProperty(ostream, "owner", obj.owner.playerId);
        writeNonemptyProperty(ostream, "kind", obj.kind);
        writeNonemptyProperty(ostream, "name", obj.name);
        writeProperty(ostream, "id", obj.id);
        writeImageXML(ostream, obj);
        if (is HasPortrait obj) {
            writeNonemptyProperty(ostream, "portrait", obj.portrait);
        }
        if (obj.empty && obj.allOrders.empty && obj.allResults.empty) {
            closeLeafTag(ostream);
        } else {
            finishParentTag(ostream);
            for (turn->orders in obj.allOrders) {
                writeOrders(ostream, "orders", turn, orders, indent + 1);
            } for (turn->results in obj.allResults) {
                writeOrders(ostream, "results", turn, results, indent + 1);
            }
            for (member in obj) {
                writeChild(ostream, member, indent + 1);
            }
            closeTag(ostream, indent, "unit");
        }
    }
    shared actual Boolean canWrite(Object obj) => obj is IUnit;
}
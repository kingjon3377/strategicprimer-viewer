import controller.map.formatexceptions {
    MissingPropertyException
}
import controller.map.misc {
    IDRegistrar
}
import controller.map.yaxml {
    YAAbstractReader
}

import java.lang {
    JIterable=Iterable,
    JAppendable=Appendable,
    JInteger=Integer,
    JNumber=Number
}
import java.math {
    BigDecimal
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import model.map.fixtures {
    ResourcePile
}

import util {
    Warning,
    Quantity
}
"A reader for resource piles."
class YAResourcePileReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<ResourcePile>(warning, idRegistrar) {
    shared actual ResourcePile read(StartElement element, QName parent,
            JIterable<XMLEvent> stream) {
        requireTag(element, parent, "resource");
        String quantityString = getParameter(element, "quantity");
        JNumber quantity;
        if (quantityString.contains(".")) {
            quantity = BigDecimal(quantityString);
        } else {
            value temp = Integer.parse(quantityString);
            if (is Integer temp) {
                quantity = JInteger(temp);
            } else {
                throw MissingPropertyException(element, "quantity", temp);
            }
        }
        ResourcePile retval = ResourcePile(getOrGenerateID(element),
            getParameter(element, "kind"), getParameter(element, "contents"),
            Quantity(quantity, getParameter(element, "unit", "")));
        if (hasParameter(element, "created")) {
            retval.created = getIntegerParameter(element, "created");
        }
        spinUntilEnd(element.name, stream);
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual Boolean isSupportedTag(String tag) => "resource" == tag.lowercased;
    shared actual void write(JAppendable ostream, ResourcePile obj, Integer indent) {
        writeTag(ostream, "resource", indent);
        writeProperty(ostream, "id", obj.id);
        writeProperty(ostream, "kind", obj.kind);
        writeProperty(ostream, "contents", obj.contents);
        writeProperty(ostream, "quantity", obj.quantity.number.string);
        writeProperty(ostream, "unit", obj.quantity.units);
        if (obj.created >= 0) {
            writeProperty(ostream, "created", obj.created);
        }
        writeImageXML(ostream, obj);
        closeLeafTag(ostream);
    }
    shared actual Boolean canWrite(Object obj) => obj is ResourcePile;
}
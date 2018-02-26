import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map.fixtures {
    ResourcePile,
    Quantity
}
import strategicprimer.model.xmlio {
    Warning
}

"A reader for resource piles."
class YAResourcePileReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<ResourcePile>(warning, idRegistrar) {
    shared actual ResourcePile read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "resource");
        expectAttributes(element, "quantity", "kind", "contents", "unit", "created", "id", "image");
        ResourcePile retval = ResourcePile(getOrGenerateID(element),
            getParameter(element, "kind"), getParameter(element, "contents"),
            Quantity(getNumericParameter(element, "quantity"),
                getParameter(element, "unit", "")));
        if (hasParameter(element, "created")) {
            retval.created = getIntegerParameter(element, "created");
        }
        spinUntilEnd(element.name, stream);
        retval.image = getParameter(element, "image", "");
        return retval;
    }
    shared actual Boolean isSupportedTag(String tag) => "resource" == tag.lowercased;
    shared actual void write(Anything(String) ostream, ResourcePile obj, Integer indent) {
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

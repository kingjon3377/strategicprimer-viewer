import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures {
    IMutableResourcePile,
    IResourcePile,
    Quantity,
    ResourcePileImpl
}
import strategicprimer.model.common.xmlio {
    Warning
}

"A reader for resource piles."
class YAResourcePileReader(Warning warning, IDRegistrar idRegistrar)
        extends YAAbstractReader<IResourcePile>(warning, idRegistrar) {
    shared actual IMutableResourcePile read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "resource");
        expectAttributes(element, "quantity", "kind", "contents", "unit", "created",
            "id", "image");
        IMutableResourcePile retval = ResourcePileImpl(getOrGenerateID(element),
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

    shared actual Boolean isSupportedTag(String tag) =>
            "resource" == tag.lowercased;
    shared actual void write(Anything(String) ostream, IResourcePile obj,
            Integer indent) {
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

    shared actual Boolean canWrite(Object obj) => obj is IResourcePile;
}

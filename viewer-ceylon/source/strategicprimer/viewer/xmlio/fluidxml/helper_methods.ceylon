import controller.map.formatexceptions {
    UnwantedChildException,
    MissingPropertyException,
    SPFormatException,
    DeprecatedPropertyException,
    SPMalformedInputException,
    UnsupportedPropertyException
}

import java.lang {
    NumberFormatException
}
import java.text {
    NumberFormat
}

import javax.xml {
    XMLConstants
}
import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamWriter,
    XMLStreamException,
    Location
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent,
    EndElement,
    Attribute,
    Characters
}

import lovelace.util.common {
    todo
}

import strategicprimer.viewer.model {
    IDRegistrar
}
import strategicprimer.viewer.model.map {
    HasImage,
    IPlayerCollection,
    HasMutableImage,
    Player
}
import strategicprimer.viewer.xmlio {
    Warning,
    spNamespace
}
NumberFormat numParser = NumberFormat.integerInstance;

"Require that an XML tag be one of the specified tags."
throws(`class SPFormatException`, "on a tag other than one, or not in a namespace, we accept")
void requireTag(
        "The tag to check."
        StartElement element,
        "The parent tag."
        QName parent,
        "The tags we accept here."
        String* tags) {
    if (!{spNamespace, XMLConstants.nullNsUri}
            .contains(element.name.namespaceURI)) {
        throw UnwantedChildException(parent, element);
    }
    String localName = element.name.localPart;
    if (!tags.contains(localName)) {
        throw UnwantedChildException(parent, element);
    }
}

"Get a parameter from the XML."
throws(`class SPFormatException`,
    "if the tag doesn't have that parameter and no default was provided")
String getAttribute(
        "The current tag."
        StartElement element,
        "The parameter we want to get."
        String param, String? defaultValue = null) {
    if (exists attr = getAttributeByName(element, param), exists retval = attr.\ivalue) {
        return retval;
    } else if (exists defaultValue) {
        return defaultValue;
    } else {
        throw MissingPropertyException(element, param);
    }
}

"Require (or recommend) that a parameter (to be subsequently retrieved via
 [[getAttribute]]) be non-empty."
throws(`class SPFormatException`, "if mandatory and missing")
void requireNonEmptyAttribute(
        "The current tag."
        StartElement element,
        "The desired parameter."
        String param,
        "Whether this is a requirement. If true, we throw the exception; if false, we
         merely warn."
        Boolean mandatory,
        "The Warning instance to use if non-mandatory."
        Warning warner) {
    if (getAttribute(element, param, "").empty) {
        SPFormatException except = MissingPropertyException(element, param);
        if (mandatory) {
            throw except;
        } else {
            warner.handle(except);
        }
    }
}

"Move along the stream until we hit an end element matching hte given start-element,
 but object to any other start elements in our namespaces."
throws(`class SPFormatException`, "on unwanted child tags")
void spinUntilEnd(
        "The tag the caller is currently parsing, whose matching end-tag we're looking
         for"
        QName tag,
        "The stream of XML."
        {XMLEvent*} reader) {
    for (event in reader) {
        if (is StartElement event, isSPStartElement(event)) {
            throw UnwantedChildException(tag, event);
        } else if (is EndElement event, tag == event.name) {
            break;
        }
    }
}

"If the specified tag has an ID as a property, return it; otherwise warn about its absence
 and generate one."
throws(`class SPFormatException`, "on SP format problems reading the property")
Integer getOrGenerateID(
        "The tag we're working with"
        StartElement element,
        "The Warning instance to use if hte tag doesn't specify an ID"
        Warning warner,
        "The factory to use to register an existing ID or get a new one"
        IDRegistrar idFactory) {
    if (hasAttribute(element, "id")) {
        try {
            return idFactory.register(
                numParser.parse(getAttribute(element, "id")).intValue(),
                warner);
        } catch (NumberFormatException|ParseException except) {
            throw MissingPropertyException(element, "id", except);
        }
    } else {
        warner.handle(MissingPropertyException(element, "id"));
        return idFactory.createID();
    }
}

"Get a parameter from an XML tag by name."
Attribute? getAttributeByName(
        "The current XML tag"
        StartElement element,
        "The parameter we want"
        String param) {
    if (exists retval = element.getAttributeByName(
        QName(spNamespace, param))) {
        return retval;
    } else if (exists retval = element.getAttributeByName(QName(param))) {
        // This case is split out because the JDK method isn't annotated @Nullable,
        // so without hinting at is nullability here Ceylon would insert an implicit
        // assert (exists).
        return retval;
    } else {
        return null;
    }
}

"Whether an XML tag has the given parameter."
Boolean hasAttribute(
        "The current tag"
        StartElement element,
        "The parameter we want"
        String param) => getAttributeByName(element, param) exists;

"If the given XML tag has the preferred parameter, return its value; if not, but it has
 the deprecated parameter, fire a warning but return its value; otherwise, throw an
 exception."
throws(`class SPFormatException`, "if the tag has neither parameter")
todo("Accept a default-value parameter and/or a type-conversion parameter")
String getAttrWithDeprecatedForm(
        "The current tag"
        StartElement element,
        "The preferred name of the parameter"
        String preferred,
        "The deprecated name of the parameter"
        String deprecated,
        "The Warning instance to use"
        Warning warner) {
    if (hasAttribute(element, preferred)) {
        return getAttribute(element, preferred);
    } else if (hasAttribute(element, deprecated)) {
        warner.handle(DeprecatedPropertyException(element, deprecated, preferred));
        return getAttribute(element, deprecated);
    } else {
        throw MissingPropertyException(element, preferred);
    }
}

"Write the given number of tabs to the given stream."
throws(`class XMLStreamException`, "on I/O error writing to the stream")
void indent(
        "The stream to write the tabs to."
        XMLStreamWriter ostream,
        "The number of tabs to write."
        Integer tabs) {
    assert (tabs >= 0);
    ostream.writeCharacters(operatingSystem.newline);
    ostream.writeCharacters("\t".repeat(tabs));
}

"If the object has a custom (non-default) image, write it to XML."
throws(`class XMLStreamException`, "on I/O error when writing")
void writeImage(
        "The stream to write to"
        XMLStreamWriter ostream,
        "The object being written out that might have a custom image"
        HasImage obj) {
    String image = obj.image;
    if (image != obj.defaultImage) {
        writeNonEmptyAttribute(ostream, "image", image);
    }
}

"Parse an integer, throwing an [[SPFormatException]] on non-numeric or otherwise malformed
 input."
throws(`class SPFormatException`, "if the string is non-numeric or otherwise malformed")
// FIXME: Use SPMalformedInputException elsewhere where we used MissingPropertyException
// for similar cases
Integer parseInt(
        "The text to parse"
        String string,
        "The current location in the XML."
        Location location) {
    try {
        return numParser.parse(string).intValue();
    } catch (ParseException except) {
        throw SPMalformedInputException(location, except);
    }
}

"Parse an Integer parameter."
todo("Replace this with a conversion function passed to [[getAttribute]]")
throws(`class SPFormatException`,
    "if the tag doesn't have that parameter and no default given, or if its value is
     non-numeric or otherwise malformed")
Integer getIntegerAttribute(
        "The tag to get the parameter from"
        StartElement tag,
        "The name of the desired parameter"
        String parameter,
        "The number to return if the parameter doesn't exist"
        Integer? defaultValue = null) {
    if (exists attr = getAttributeByName(tag, parameter), exists val = attr.\ivalue) {
        return parseInt(val, tag.location);
    } else if (exists defaultValue) {
        return defaultValue;
    } else {
        throw MissingPropertyException(tag, parameter);
    }
}

"Write the necessary number of tab characters and a tag."
throws(`class XMLStreamException`, "on I/O error writing to the stream")
void writeTag(
        "The stream to write to"
        XMLStreamWriter ostream,
        "The tag to write"
        String tag,
        "The indentation level. If positive, write a newline before indenting; if zero,
         write a default-namespace declaration."
        Integer indentation,
        "Whether to automatically close the tag"
        Boolean leaf) {
    assert (indentation >= 0);
    if (indentation > 0) {
        indent(ostream, indentation);
    }
    if (leaf) {
        ostream.writeEmptyElement(spNamespace, tag);
    } else {
        ostream.writeStartElement(spNamespace, tag);
    }
    if (indentation == 0) {
        ostream.writeDefaultNamespace(spNamespace);
    }
}

"Write an attribute to XML."
throws(`class XMLStreamException`, "on I/O error")
todo("Convert to writeAttributes(XMLStreamWriter, <String->String>*)")
void writeAttribute(
        "The stream to write to"
        XMLStreamWriter ostream,
        "The name of the attribute to write"
        String name,
        "The value of the attribute"
        String item) => ostream.writeAttribute(spNamespace, name, item);

"Write an attribute whose value is an integer."
throws(`class XMLStreamException`, "on I/O error")
void writeIntegerAttribute(
        "The stream to write to"
        XMLStreamWriter ostream,
        "The name of the attribute to write"
        String name,
        "The value of the attribute"
        Integer item) => writeAttribute(ostream, name, item.string);

"Write an attribute if its value is nonempty."
throws(`class XMLStreamException`, "on I/O error")
void writeNonEmptyAttribute(
        "The stream to write to"
        XMLStreamWriter ostream,
        "The name of the attribute to write."
        String name,
        "The value of the attribute"
        String item) {
    if (!item.empty) {
        ostream.writeAttribute(spNamespace, name, item);
    }
}

"Write an attribute whose value is a Boolean value."
throws(`class XMLStreamException`, "on error creating XML")
void writeBooleanAttribute(
        "The stream to write to"
        XMLStreamWriter ostream,
        "The name of the attribute to write"
        String name,
        "Its value"
        Boolean item) => writeAttribute(ostream, name, item.string);

"""If the specified tag has an "owner" property, return the player it indicates; otherwise
   warn about its absence and return the "independent" player from the player
   collection."""
throws(`class SPFormatException`, "on SP format problems reading the attribute")
Player getPlayerOrIndependent(
        "The tag we're working with"
        StartElement element,
        "The Warning instance to use"
        Warning warner,
        "The collection of players to refer to"
        IPlayerCollection players) {
    if (hasAttribute(element, "owner")) {
        return players.getPlayer(getIntegerAttribute(element, "owner"));
    } else {
        warner.handle(MissingPropertyException(element, "owner"));
        return players.independent;
    }
}

"Set an object's image property if an image filename is specified in the XML."
Type setImage<Type>(
        "The object in question"
        Type obj,
        "The current XML tag"
        StartElement element,
        "The Warning instance to use if the object can't have an image but the XML
         specifies one"
        Warning warner) {
    if (is HasMutableImage obj) {
        obj.image = getAttribute(element, "image", "");
    } else if (hasAttribute(element, "image")) {
        warner.handle(UnsupportedPropertyException(element, "image"));
    }
    return obj;
}

"Whether the given XML element is a [[StartElement]] and in a namespace we support."
Boolean isSPStartElement(XMLEvent element) {
    if (is StartElement element,
            {spNamespace, XMLConstants.nullNsUri}
                .contains(element.name.namespaceURI)) {
        return true;
    } else {
        return false;
    }
}

"Get the text between here and the closing tag we're looking for; any intervening tags
 cause an exception."
throws(`class SPFormatException`, "on unwanted intervening tags")
String getTextUntil(
        "The name of the tag whose closing tag we're waitng for"
        QName tag,
        "The stream of XML elements to sift through"
        {XMLEvent*} stream) {
    StringBuilder builder = StringBuilder();
    for (event in stream) {
        if (is StartElement event, isSPStartElement(event)) {
            throw UnwantedChildException(tag, event);
        } else if (is Characters event) {
            builder.append(event.data);
        } else if (is EndElement event, tag == event.name) {
            break;
        }
    }
    return builder.string.trimmed;
}
import java.lang {
    IllegalArgumentException
}
import java.text {
    NumberFormat,
    JParseException=ParseException
}

import javax.xml {
    XMLConstants
}
import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    Location
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent,
    EndElement,
    Attribute
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map {
    Point,
    HasImage,
    pointFactory
}
import strategicprimer.model.xmlio {
    spNamespace,
    Warning,
    SPFormatException
}
import strategicprimer.model.xmlio.exceptions {
    UnwantedChildException,
    MissingPropertyException,
    DeprecatedPropertyException
}
"A parser for numeric data, so integers can contain commas."
NumberFormat numParser = NumberFormat.integerInstance;
"Patterns to match XML metacharacters, and their qutoed forms."
{[String, String]*} quoting = {["""&""", """&amp;"""], ["""<""", """&lt;"""],
    [""">""", """&gt;"""], [""""""", """&quot;"""], ["""'""", """&apos;"""]};
"A superclass for YAXML reader classes to provide helper methods."
abstract class YAAbstractReader<Element>
        satisfies YAReader<Element> given Element satisfies Object {
    "Whether the given tag is in a namespace we support."
    static Boolean isSupportedNamespace(QName tag) =>
            {spNamespace, XMLConstants.nullNsUri}.contains(tag.namespaceURI);
    "Require that an element be one of the specified tags."
    shared static void requireTag(StartElement element, QName parent, String* tags) {
        if (!isSupportedNamespace(element.name)) {
            throw UnwantedChildException(parent, element,
                IllegalArgumentException("Unrecognized namespace"));
        }
        String localName = element.name.localPart;
        Integer line = element.location.lineNumber;
        if (!tags.contains(localName)) {
            // While we'd like tests to exercise this, we're always careful to only call
            // readers when we know they support the tag ...
            throw UnwantedChildException(parent, element, IllegalArgumentException(
                "Unexpected tag ``localName`` on line ``line``, expected one of these: ``
                ", ".join(tags)``"));
        }
    }
    "Create a [[QName]] for the given tag in our namespace."
    static QName qname(String tag) => QName(spNamespace, tag);
    "Get an attribute by name from the given tag, if it's there."
    static Attribute? getAttributeByName(StartElement element, String parameter) {
        if (exists retval = element.getAttributeByName(qname(parameter))) {
            return retval;
        } else if (exists retval = element.getAttributeByName(QName(parameter))) {
            return retval;
        } else {
            return null;
        }
    }
    "Read a parameter (aka attribute aka property) from an XML tag."
    shared static String getParameter(StartElement element, String param,
            String? defaultValue = null) {
        if (exists attr = getAttributeByName(element, param),
            exists retval = attr.\ivalue) {
            return retval;
        } else if (exists defaultValue) {
            return defaultValue;
        } else {
            throw MissingPropertyException(element, param);
        }
    }
    "Whether the given XML event is an end element matching the given tag."
    shared static Boolean isMatchingEnd(QName tag, XMLEvent event) {
        if (is EndElement event) {
            return tag == event.name;
        } else {
            return false;
        }
    }
    "Whether the given XMLElement is a [[StartElement]] in a namespace we support."
    todo("Inline into callers, since they now have to use `is StartElement` anyway")
    shared static Boolean isSPStartElement(XMLEvent element) {
        if (is StartElement element) {
            return isSupportedNamespace(element.name);
        } else {
            return false;
        }
    }
    "Advance the stream until we hit an end element matching the given name, but object to
     any start elements."
    shared static void spinUntilEnd(QName tag, {XMLEvent*} reader) {
        for (event in reader) {
            if (is StartElement event, isSPStartElement(event)) {
                throw UnwantedChildException(tag, event);
            } else if (isMatchingEnd(tag, event)) {
                break;
            }
        }
    }
    "Whether the given tag has the given parameter."
    shared static Boolean hasParameter(StartElement element, String param) =>
            getAttributeByName(element, param) exists;
    "Append the given number of tabs to the stream."
    shared static void indent(Anything(String) ostream, Integer tabs) {
        ostream("\t".repeat(tabs));
    }
    "Replace XML meta-characters in a string with their equivalents."
    shared static String simpleQuote(String text) {
        variable String retval = text;
        for ([pattern, replacement] in quoting) {
            retval = retval.replace(pattern, replacement);
        }
        return retval;
    }
    "Write a property to XML."
    shared static void writeProperty(Anything(String) ostream, String name,
            String|Integer val) {
        switch (val)
        case (is String) {
            ostream(" ``simpleQuote(name)``=\"``simpleQuote(val)``\"");
        }
        case (is Integer) { writeProperty(ostream, name, val.string); }
    }
    "Write a property to XML only if its value is nonempty."
    shared static void writeNonemptyProperty(Anything(String) ostream, String name,
            String val) {
        if (!val.empty) {
            writeProperty(ostream, name, val);
        }
    }
    "Write the image property to XML if the object's image is nonempty and differs from
     the default."
    shared static void writeImageXML(Anything(String) ostream, HasImage obj) {
        String image = obj.image;
        if (image != obj.defaultImage) {
            writeNonemptyProperty(ostream, "image", image);
        }
    }
    "Parse an integer. We use [[NumberFormat]] rather than [[Integer.parse]] because we
     want to allow commas in the input."
    todo("Test that commas in the input are allowed",
        "Inline this into the caller or pass in information that lets us throw a more
         meaningful exception, so we can get rid of SPMalformedInputException")
    throws(`class JParseException`, "on non-numeric input")
    throws(`class ParseException`,
        "on non-numeric input, if we were using [[Integer.parse]]")
    static Integer parseInt(String string,
            "The current location in the document" Location location) {
        return numParser.parse(string).intValue();
    }
    "Read a parameter from XML whose value must be an integer."
    shared static Integer getIntegerParameter(StartElement element, String parameter,
            Integer? defaultValue = null) {
        if (exists attr = getAttributeByName(element, parameter),
            exists retval = attr.\ivalue) {
            try {
                return parseInt(retval, element.location);
            } catch (ParseException|JParseException except) {
                throw MissingPropertyException(element, parameter, except);
            }
        } else if (exists defaultValue) {
            return defaultValue;
        } else {
            throw MissingPropertyException(element, parameter);
        }
    }
    "Write the necessary number of tab characters and a tag. Does not write the
     right-bracket to close the tag. If `tabs` is 0, emit a namespace declaration as
     well."
    shared static void writeTag(Anything(String) ostream, String tag, Integer tabs) {
        indent(ostream, tabs);
        ostream("<``simpleQuote(tag)``");
        if (tabs == 0) {
            ostream(" xmlns=\"``spNamespace``\"");
        }
    }
    "Close a tag with a right-bracket and add a newline."
    shared static void finishParentTag(Anything(String) ostream) =>
            ostream(">``operatingSystem.newline``");
    "Close a 'leaf' tag and add a newline."
    shared static void closeLeafTag(Anything(String) ostream) =>
            ostream(" />``operatingSystem.newline``");
    "Write a closing tag to the stream, optionally indented, and followed by a
     newline."
    shared static void closeTag(Anything(String) ostream, Integer tabs, String tag) {
        if (tabs > 0) {
            indent(ostream, tabs);
        }
        ostream("</``simpleQuote(tag)``>``operatingSystem.newline``");
    }
    "Parse a Point from a tag's properties."
    shared static Point parsePoint(StartElement element) =>
            pointFactory(getIntegerParameter(element, "row"),
                getIntegerParameter(element, "column"));
    "The Warning instance to use."
    Warning warner;
    "The factory for ID numbers"
    IDRegistrar idf;
    shared new (Warning warning, IDRegistrar idRegistrar) {
        warner = warning;
        idf = idRegistrar;
    }
    "Read a parameter from XML whose value must be a boolean."
    shared Boolean getBooleanParameter(StartElement element, String parameter,
            Boolean? defaultValue = null) {
        if (exists attr = getAttributeByName(element, parameter),
                exists val = attr.\ivalue) {
            value retval = Boolean.parse(val);
            if (is Boolean retval) {
                return retval;
            } else if (exists defaultValue) {
                warner.handle(retval); // TODO: Wrap to report XML location?
                return defaultValue;
            } else {
                throw retval;
            }
        } else if (exists defaultValue) {
            return defaultValue;
        } else {
            throw MissingPropertyException(element, parameter);
        }
    }
    "Require that a parameter be present and non-empty."
    shared void requireNonEmptyParameter(StartElement element, String parameter,
            Boolean mandatory) {
        if (getParameter(element, parameter, "").empty) {
            SPFormatException except = MissingPropertyException(element, parameter);
            if (mandatory) {
                throw except;
            } else {
                warner.handle(except);
            }
        }
    }
    "Register the specified ID number, and return it."
    todo("Is the 'return it' feature ever used?")
    shared Integer registerID(Integer id) => idf.register(id, warner);
    "If the specified tag has an ID as a property, return it; otherwise, warn about its
     absence and generate one."
    shared Integer getOrGenerateID(StartElement element) {
        if (hasParameter(element, "id")) {
            return registerID(getIntegerParameter(element, "id"));
        } else {
            warner.handle(MissingPropertyException(element, "id"));
            return idf.createID();
        }
    }
    "Get a parameter from an element in its preferred form, if present, or in a deprecated
     form, in which case fire a warning."
    shared String getParamWithDeprecatedForm(StartElement element, String preferred,
            String deprecated) {
        if (exists preferredProperty = getAttributeByName(element, preferred),
                exists retval = preferredProperty.\ivalue) {
            return retval;
        } else if (exists deprecatedProperty = getAttributeByName(element, deprecated),
                exists retval = deprecatedProperty.\ivalue) {
            warner.handle(DeprecatedPropertyException(element, deprecated, preferred));
            return retval;
        } else {
            throw MissingPropertyException(element, preferred);
        }
    }
}
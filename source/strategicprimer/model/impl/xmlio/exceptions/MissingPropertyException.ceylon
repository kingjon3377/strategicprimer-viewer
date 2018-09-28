import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement
}

import strategicprimer.model.impl.xmlio {
    SPFormatException
}
"An exception for cases where a parameter is required (or, if this is merely logged,
 recommended) but missing."
shared class MissingPropertyException("The current tag." StartElement context, param,
        "The underlying cause" Throwable? cause = null)
        extends SPFormatException(
            "Missing parameter ``param`` in tag ``context.name.localPart``",
            context.location.lineNumber, context.location.columnNumber, cause) {
    "The current tag."
    shared QName tag = context.name;
    "The missing parameter."
    shared String param;
}

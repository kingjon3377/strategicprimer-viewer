import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement
}

import strategicprimer.model.common.xmlio {
    SPFormatException
}
"A custom exception for cases where one property is deprecated in favor of another."
shared class DeprecatedPropertyException(context, old, preferred)
        extends SPFormatException(
            "Use of the property ``old`` in tag ``context.name
                .localPart`` is deprecated; use ``preferred`` instead",
            context.location.lineNumber, context.location.columnNumber) {
    "The current tag."
    StartElement context;
    "The current tag."
    shared QName tag = context.name;
    "The old form of the property"
    shared String old;
    "The preferred form of the property"
    shared String preferred;
}

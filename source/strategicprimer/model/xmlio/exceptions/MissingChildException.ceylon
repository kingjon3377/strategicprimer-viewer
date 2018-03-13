import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement
}

import strategicprimer.model.xmlio {
    SPFormatException
}
"A custom exception for when a tag requires a child and it isn't there."
shared class MissingChildException(context)
        extends SPFormatException("Tag ``context.name.localPart`` missing a child",
            context.location.lineNumber, context.location.columnNumber) {
    "The current tag (the one that needs a child"
    StartElement context;
    "The current tag"
    shared QName tag = context.name;
}

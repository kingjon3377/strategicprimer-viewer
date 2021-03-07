import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement
}

import strategicprimer.model.common.xmlio {
    SPFormatException
}

"A custom exception for cases where a tag has a property it doesn't support."
shared class UnsupportedPropertyException extends SPFormatException {
    "The unsupported property."
    shared String param;

    "The current tag."
    shared QName tag;

    shared new (StartElement context, String param) extends SPFormatException(
            "Unsupported property ``param`` in tag ``context.name.localPart``",
            context.location.lineNumber, context.location.columnNumber) {
        this.param = param;
        tag = context.name;
    }

    "A variation for when a property is *conditionally* supported."
    shared new inContext(StartElement tag, String param, String context)
            extends SPFormatException(
                "Unsupported property ``param`` in tag ``tag.name.localPart`` ``context``",
                tag.location.lineNumber, tag.location.columnNumber) {
        this.param = param;
        this.tag = tag.name;
    }
}

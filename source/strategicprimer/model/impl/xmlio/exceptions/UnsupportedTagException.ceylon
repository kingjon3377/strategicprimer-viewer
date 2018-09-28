import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement
}

import strategicprimer.model.impl.xmlio {
    SPFormatException
}
"A custom exception for not-yet-supported tags."
shared class UnsupportedTagException extends SPFormatException {
    "The unsupported tag."
    shared QName tag;
    shared new future(StartElement unexpectedTag)
            extends SPFormatException("Unexpected tag ``unexpectedTag.name
                    .localPart``; probably a more recent map format than we support",
                unexpectedTag.location.lineNumber, unexpectedTag.location.columnNumber) {
        tag = unexpectedTag.name;
    }
    shared new obsolete(StartElement unexpectedTag)
            extends SPFormatException(
                "No-longer-supported tag ``unexpectedTag.name.localPart``",
                unexpectedTag.location.lineNumber, unexpectedTag.location.columnNumber) {
        tag = unexpectedTag.name;
    }
}

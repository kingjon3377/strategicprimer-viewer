import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement
}

import strategicprimer.model.xmlio {
    SPFormatException
}
"A custom exception for not-yet-supported tags."
shared class UnsupportedTagException(StartElement unexpectedTag)
        extends SPFormatException("Unexpected tag ``unexpectedTag.name
                .localPart``; probably a more recent map format than we support",
            unexpectedTag.location.lineNumber, unexpectedTag.location.columnNumber) {
    "The unsupported tag."
    shared QName tag = unexpectedTag.name;
}

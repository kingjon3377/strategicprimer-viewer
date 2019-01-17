import javax.xml.stream.events {
    StartElement
}

import strategicprimer.model.common.xmlio {
    SPFormatException
}

"An exception to indicate that a map file specified a map version not supported by the
 code reading it."
shared class MapVersionException extends SPFormatException {
    static String messageFragment(Integer minimum, Integer maximum) {
        if (minimum == maximum) {
            return ": must be " + minimum.string;
        } else {
            return ": must be between ``minimum`` and ``maximum``";
        }
    }

    shared new ("The current tag." StartElement context,
        "The requested map version." Integer version,
        "The lowest version the code supports" Integer minimum,
        "The highest version the code supports" Integer maximum)
        extends SPFormatException(
            "Unsupported SP map version ``version`` in tag " + context.name.localPart +
                messageFragment(minimum, maximum),
            context.location.lineNumber, context.location.columnNumber) {}

    shared new nonXML("The requested map version." Integer version,
        "The lowest version the code supports" Integer minimum,
        "The highest version the code supports" Integer maximum)
        extends SPFormatException("Unsupported SP map version " + version.string +
            messageFragment(minimum, maximum), -1, -1) {}
}

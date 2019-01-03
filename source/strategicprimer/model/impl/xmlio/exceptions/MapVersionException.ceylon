import javax.xml.stream.events {
    StartElement
}

import strategicprimer.model.common.xmlio {
    SPFormatException
}

"An exception to indicate that a map file specified a map version not supported by the
 code reading it."
shared class MapVersionException(
        "The current tag."
        StartElement context,
        "The requested map version."
        Integer version,
        "The lowest version the code supports"
        Integer minimum,
        "The highest version the code supports"
        Integer maximum) extends SPFormatException(
            "Unsupported SP map version ``version`` in tag ``context.name.localPart
                ``: must be between ``minimum`` and ``maximum``", // TODO: Avoid "between" when min == max
                context.location.lineNumber, context.location.columnNumber) {
}

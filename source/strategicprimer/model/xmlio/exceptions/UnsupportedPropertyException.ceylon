import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement
}

import strategicprimer.model.xmlio {
    SPFormatException
}
"A custom exception for cases where a tag has a property it doesn't support."
shared class UnsupportedPropertyException(StartElement context, param)
		extends SPFormatException(
			"Unsupported property ``param`` in tag ``context.name.localPart``",
			context.location) {
	"The unsupported property."
	shared String param;
	"The current tag."
	shared QName tag = context.name;
}
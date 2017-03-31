import javax.xml.stream {
    Location
}

import lovelace.util.common {
    todo
}
"For cases of malformed input where we can't use [[XMLStreamException]]."
todo("Eliminate, since Ceylon doesn't have checked exception")
deprecated("Unnecessary now that everything's in Ceylon")
shared class SPMalformedInputException(Location location, Throwable cause)
		extends SPFormatException("Malformed input", location, cause) {}
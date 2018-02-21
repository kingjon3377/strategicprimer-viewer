import java.io {
    JReader=Reader,
    StringReader,
    FileReader
}

"""Get the appropriate reader based on the given filename: if it begins "string:<", strip
   the "string:" prefix, pass the remainder to a [[StringReader]], and return it; otherwise
   return a [[FileReader]]."""
JReader magicReader(String possibleFilename) {
	if (possibleFilename.startsWith("string:<")) {
		return StringReader(possibleFilename.substring(7));
	} else {
		return FileReader(possibleFilename);
	}
}
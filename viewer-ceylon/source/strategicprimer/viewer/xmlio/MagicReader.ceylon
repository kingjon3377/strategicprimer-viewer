import java.io {
    JReader=Reader,
    StringReader,
    FileReader
}

import lovelace.util.common {
    todo
}
import java.lang {
    CharArray
}
"""A Reader that delegates to FileReader unless the filename begins "string:<", in which
   case the "string:" prefix is stripped and we delegate to a StringReader."""
todo("Replace with a factory function")
class MagicReader extends JReader {
    JReader delegate;
    String filename;
    shared new (String possibleFilename) extends JReader() {
        if (possibleFilename.startsWith("string:<")) {
            delegate = StringReader(possibleFilename.substring(7));
            filename = "a string";
        } else {
            delegate = FileReader(possibleFilename);
            filename = possibleFilename;
        }
    }
    shared actual Integer read(CharArray buffer, Integer offset, Integer length) =>
            delegate.read(buffer, offset, length);
    shared actual void close() => delegate.close();
    shared actual Integer read() => delegate.read();
    shared actual Boolean markSupported() => delegate.markSupported();
    shared actual void mark(Integer readAheadLimit) => delegate.mark(readAheadLimit);
    shared actual void reset() => delegate.reset();
}
import java.io {
    IOException
}

import strategicprimer.model.map {
    IMap
}
import strategicprimer.model.xmlio {
    SPWriter
}
import ceylon.file {
    Path,
    File,
    Nil
}
"Sixth generation SP XML writer."
shared object yaXMLWriter satisfies SPWriter {
    "Write an object to a stream."
    throws(`class IOException`, "on I/O error")
    shared actual void writeSPObject("The stream to write to" Path|Anything(String) arg,
            "The object to write" Object obj) {
        if (is Anything(String) ostream = arg) {
            YAReaderAdapter().write(ostream, obj, 0);
        } else if (is Path path = arg) {
            File file;
            value res = path.resource;
            if (is File res) {
                file = res;
            } else if (is Nil res) {
                file = res.createFile();
            } else {
                throw IOException("``path`` exists but is not a file");
            }
            try (writer = file.Overwriter()) {
                writeSPObject(writer.write, obj);
            }
        }
    }
    "Write a map to a file or stream."
    throws(`class IOException`, "on I/O error")
    shared actual void write("The file to write to." Path|Anything(String) arg,
            "The map to write." IMap map) => writeSPObject(arg, map);
}
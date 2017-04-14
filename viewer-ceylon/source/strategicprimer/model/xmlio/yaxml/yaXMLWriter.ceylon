import java.io {
    IOException
}
import java.nio.file {
    JPath=Path,
    JFiles=Files
}

import strategicprimer.model.map {
    IMapNG
}
import strategicprimer.model.xmlio {
    SPWriter
}
"Sixth generation SP XML writer."
shared object yaXMLWriter satisfies SPWriter {
    "Write an object to a stream."
    throws(`class IOException`, "on I/O error")
    shared actual void writeSPObject("The stream to write to" JPath|Anything(String) arg,
            "The object to write" Object obj) {
        if (is Anything(String) ostream = arg) {
            YAReaderAdapter().write(ostream, obj, 0);
        } else if (is JPath file = arg) {
            try (writer = JFiles.newBufferedWriter(file)) {
                writeSPObject((String str) => writer.append(str), obj);
            }
        }
    }
    "Write a map to a file or stream."
    throws(`class IOException`, "on I/O error")
    shared actual void write("The file to write to." JPath|Anything(String) arg,
            "The map to write." IMapNG map) => writeSPObject(arg, map);
}
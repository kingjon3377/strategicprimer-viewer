import controller.map.iointerfaces {
    SPWriter
}
import controller.map.yaxml {
    YAReaderAdapter
}

import java.io {
    IOException
}
import java.lang {
    JAppendable=Appendable
}
import java.nio.file {
    JPath=Path
}

import model.map {
    IMapNG
}
"Sixth generation SP XML writer."
shared object yaXMLWriter satisfies SPWriter {
    "Write an object to a stream."
    throws(`class IOException`, "on I/O error")
    shared actual void writeSPObject("The stream to write to" JAppendable ostream,
            "The object to write" Object obj) => YAReaderAdapter().write(ostream, obj, 0);
    "Write a map to file."
    throws(`class IOException`, "on I/O error")
    shared actual void write("The file to write to." JPath file,
            "The map to write." IMapNG map) => super.writeSPObject(file, map);
    "Write a map to a stream."
    throws(`class IOException`, "on I/O error")
    shared actual void write("The stream to write to" JAppendable ostream,
            "The map to write" IMapNG map) => writeSPObject(ostream, map);

}
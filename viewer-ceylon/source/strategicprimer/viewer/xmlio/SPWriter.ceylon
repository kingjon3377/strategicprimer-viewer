import java.lang {
    JAppendable=Appendable
}
import java.nio.file {
    JPath=Path
}

import lovelace.util.common {
    todo
}

import strategicprimer.viewer.model.map {
    IMapNG
}
"An interface for map (and other SP XML) writers."
shared interface SPWriter {
    "Write a map to file or a stream."
    shared formal void write(
            "The file or stream to write to."
            todo("Take String() instead of Appendable")
            JPath|JAppendable arg,
            "The map to write"
            IMapNG map);
    "Write an object to a file or stream."
    shared formal void writeSPObject(JPath|JAppendable arg, Object obj);
}
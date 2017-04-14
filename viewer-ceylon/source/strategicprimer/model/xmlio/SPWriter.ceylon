import java.nio.file {
    JPath=Path
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    IMapNG
}
"An interface for map (and other SP XML) writers."
shared interface SPWriter {
    "Write a map to file or a stream."
    todo("Port to ceylon.file")
    shared formal void write(
            "The file or stream to write to."
            JPath|Anything(String) arg,
            "The map to write"
            IMapNG map);
    "Write an object to a file or stream."
    shared formal void writeSPObject(JPath|Anything(String) arg, Object obj);
}
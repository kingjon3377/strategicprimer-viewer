import ceylon.file {
    Path
}

import strategicprimer.model.common.map {
    IMapNG
}
"An interface for map (and other SP XML) writers."
shared interface SPWriter {
    "Write a map to file or a stream."
    shared formal void write(
            "The file or stream to write to."
            Path|Anything(String) arg,
            "The map to write"
            IMapNG map);
    "Write an object to a file or stream."
    shared formal void writeSPObject(Path|Anything(String) arg, Object obj);
}

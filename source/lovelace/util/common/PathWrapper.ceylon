import ceylon.file {
    Path,
    parsePath
}
"A wrapper around a filename." // TODO: Replace with ceylon.file::Path once eclipse/ceylon-sdk#239 makes it non-JVM-specific
shared native class PathWrapper(shared String filename) {
    shared actual native Boolean equals(Object other) {
        if (is PathWrapper other) {
            return other.filename == filename;
        } else {
            return false;
        }
    }
    shared actual String string => filename;
    shared actual native Integer hash => filename.hash;
    // TODO: Add a 'possiblyReadable' attribute, defaulting to 'true' in the native-default, for ReaderComparator if no other driver
}
shared native("jvm") class PathWrapper(shared String filename) {
    Path path = parsePath(filename);
    shared actual native("jvm") Boolean equals(Object other) {
        if (is PathWrapper other) {
            return other.path == path;
        } else {
            return false;
        }
    }
    shared actual native("jvm") Integer hash => path.hash;
}
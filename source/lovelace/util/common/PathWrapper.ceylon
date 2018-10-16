import ceylon.file {
    Path,
    parsePath,
    File
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
    "False if we know the path represented by this instance will not be
     readable (usually because there is not a file by that name); true
     if it would be, or if this class has no way of knowing. (In the
     default implementation this attribute always returns true, since
     ceylon.file is JVM-only.)"
    shared native Boolean possiblyReadable => true;
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
    shared native("jvm") Boolean possiblyReadable => path.resource is File;
}

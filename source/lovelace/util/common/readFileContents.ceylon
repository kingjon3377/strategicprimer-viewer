import ceylon.language.meta.declaration {
    Module
}
import ceylon.file {
    Nil,
    File,
    Directory,
    Link,
    FilesystemResource=Resource,
    parsePath
}

"Get the contents of a file, which may be in the filesystem (if on a platform for which
 Ceylon provides a suitable interface for that, i.e. `ceylon.file` is supported) or a 
 resource in the given module (which is always supported), as a String. Returns null 
 if file not found in either place."
shared native String? readFileContents(Module sourceModule, String filename) {
    if (exists file = sourceModule.resourceByPath(filename)) {
        return file.textContent();
    } else {
        return null;
    }
}

shared native("jvm") String? readFileContents(Module sourceModule, String filename) {
    File? getFilesystemResource(FilesystemResource res) {
        switch (res)
        case (is Nil|Directory) { return null; }
        case (is File) { return res; }
        case (is Link) { return getFilesystemResource(res.linkedResource); }
    }
    if (exists file = getFilesystemResource(parsePath(filename).resource)) {
        StringBuilder builder = StringBuilder();
        try (reader = file.Reader()) {
            while (exists line = reader.readLine()) {
                builder.append(line);
                builder.appendNewline();
            }
        }
        return builder.string;
    } else if (exists file = sourceModule.resourceByPath(filename)) {
        return file.textContent();
    } else {
        return null;
    }
}

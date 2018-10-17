import java.io {
    FileInputStream,
    FileNotFoundException,
    InputStream
}
import java.lang {
    Thread { currentThread }
}
import lovelace.util.common {
    todo
}

"Basically a FileInputStream, but the file could be on disk or in the classpath.
 This method replaces the Java class (I wrote) ResourceInputStream."
todo("Remove, since this doesn't actually work, and [[ResourceInputStream]] at
      least sort of does.")
shared InputStream loadResource(String filename) {
    try {
        return FileInputStream(filename);
    } catch (FileNotFoundException except) {
        // We want to use `module strategicprimer.viewer`.resourceByPath(filename) , but
        // there's no API for *binary* contents of a Resource ... but this still doesn't
        // work ...
        // FIXME: What prefix do we need?
        if (exists retval = currentThread().contextClassLoader
                .getResourceAsStream("``filename``")) {
            return retval;
        } else {
            throw except;
        }
    }
}

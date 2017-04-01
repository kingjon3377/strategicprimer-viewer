import ceylon.interop.java {
    javaClass,
    javaClassFromInstance
}
import ceylon.language.meta {
    modules
}

import java.io {
    InputStream,
    BufferedInputStream,
    FileInputStream,
    FileNotFoundException
}
import java.lang {
    ByteArray
}

import lovelace.util.common {
    todo
}
"Basically a [[FileInputStream]], but the file could be on disk or in the classpath."
todo("Test how this works with Ceylon 'resources'",
    "Use Ceylon's metamodel when it supports binary access")
shared class ResourceInputStream(String filename) extends InputStream() {
    InputStream factory() {
        try {
            return BufferedInputStream(FileInputStream(filename));
        } catch (FileNotFoundException except) {
            if (exists temp =
                    javaClass<ResourceInputStream>().getResourceAsStream(filename)) {
                return temp;
            } else {
//                if (exists uri = mod.resourceByPath(filename)?.uri, exists temp =
//                        javaClass<ResourceInputStream>()
//                            .getResourceAsStream(uri.string)) {
                for (mod in modules.list) {
                    if (exists temp = javaClassFromInstance(mod).getResourceAsStream(
                            "/``mod.name.replace(".", "/")``/``filename``")) {
                        return temp;
                    }
                }
                throw except;
            }
        }
    }
    InputStream wrapped = factory();
    "Read a single byte from the wrapped stream."
    shared actual Integer read() => wrapped.read();
    "Read from the wrapped stream into a provided buffer."
    shared actual Integer read(ByteArray buf) => wrapped.read(buf);
    "Read from the wrapped stream into a provided buffer at some offset."
    shared actual Integer read(ByteArray buf, Integer off, Integer len) =>
            wrapped.read(buf, off, len);
    "Skip some bytes in the wrapped stream."
    shared actual Integer skip(Integer num) => wrapped.skip(num);
    "How many bytes are estimated to be available in the wrapped stream."
    shared actual Integer available() => wrapped.available();
    "Close the wrapped stream."
    shared actual void close() => wrapped.close();
}
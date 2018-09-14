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
    ByteArray,
    JClass=Class,
    Types,
    overloaded
}

import lovelace.util.common {
    todo
}
import ceylon.language.meta.declaration {
    Module
}
variable Boolean first = false;
"Basically a [[FileInputStream]], but the file could be on disk or in the classpath."
todo("Test how this works with Ceylon 'resources'",
    "Use Ceylon's metamodel when it supports binary access")
shared class ResourceInputStream(String filename,
        Module sourceModule=`module lovelace.util.jvm`,
        JClass<out Object> sourceClass=Types.classForType<ResourceInputStream>())
        extends InputStream() {
    InputStream factory() {
        try {
            return BufferedInputStream(FileInputStream(filename));
        } catch (FileNotFoundException except) {
            if (exists temp =
                    sourceClass.getResourceAsStream(filename)) {
                return temp;
            } else if (exists temp = sourceClass.getResourceAsStream(
                "/``sourceModule.name.replace(".", "/")``/``filename``")) {
                return temp;
            } else if (exists temp = sourceClass.getResourceAsStream(
                "``sourceModule.name.replace(".", "/")``/``filename``")) {
                return temp;
//            } else if (exists uri = sourceModule.resourceByPath(filename)?.uri,
//                    exists temp = Types.classForType<ResourceInputStream>()
//                        .getResourceAsStream(uri.string)) {
//                return temp;
            } else if (exists uri = sourceModule.resourceByPath(filename)?.uri) {
                if (first) {
                    process.writeLine("URI is '``uri``'");
                    first = false;
                }
                if (exists temp = sourceClass
                        .getResourceAsStream(uri)) {
                    return temp;
                } else if (exists temp = sourceClass
                        .getResourceAsStream(uri.replaceFirst("classpath:/", "/"))) {
                    return temp;
                }
            }
            for (mod in modules.list) {
                if (exists temp = Types.classForInstance(mod).getResourceAsStream(
                        "/``mod.name.replace(".", "/")``/``filename``")) {
                    return temp;
                } else if (exists temp =
                            Types.classForInstance(mod).getResourceAsStream(
                        "``mod.name.replace(".", "/")``/``filename``")) {
                    return temp;
                }
            }
            throw except;
        }
    }
    InputStream wrapped = factory();
    "Read a single byte from the wrapped stream."
    shared actual overloaded Integer read() => wrapped.read();
    "Read from the wrapped stream into a provided buffer."
    shared actual overloaded Integer read(ByteArray buf) => wrapped.read(buf);
    "Read from the wrapped stream into a provided buffer at some offset."
    shared actual overloaded Integer read(ByteArray buf, Integer off, Integer len) =>
            wrapped.read(buf, off, len);
    "Skip some bytes in the wrapped stream."
    shared actual Integer skip(Integer num) => wrapped.skip(num);
    "How many bytes are estimated to be available in the wrapped stream."
    shared actual Integer available() => wrapped.available();
    "Close the wrapped stream."
    shared actual void close() => wrapped.close();
}

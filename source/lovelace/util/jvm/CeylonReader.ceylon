import java.io {
    JReader=Reader
}
import ceylon.buffer.readers {
    Reader
}
import java.lang {
    CharArray,
    overloaded
}

"An adapter from [[the Ceylon SDK's Reader interface|Reader]] to [[the Java
 Reader API|JReader]] (which Java STaX requires).  This will almost certainly
 be much slower than the Java SDK's implementations, unfortunately."
shared class CeylonReader(Reader wrapped) extends JReader() {
    "Read a single byte."
    shared actual overloaded Integer read() {
        if (exists retval = wrapped.readByte()) {
            return retval.unsigned;
        } else {
            return -1;
        }
    }
    "Because [[the Ceylon Reader interface|Reader]] does not provide a close()
     method, this does nothing."
    shared actual void close() {}
    "Read a series of bytes into an array. Because of the limitations of [[the
     Ceylon Reader API|Reader]], it reads them one byte at a time."
    shared actual overloaded Integer read(CharArray cbuf, Integer offset,
            Integer length) {
        for (i in 0:offset) {
            if (!wrapped.readByte() exists) {
                return -1;
            }
        }
        variable Integer count = 0;
        for (i in 0:Integer.smallest(cbuf.size, length)) {
            if (exists byte = wrapped.readByte()) {
                cbuf.set(i, byte.unsigned.character);
                count++;
            } else {
                return -1;
            }
        }
        return count;
    }
}

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
"An adapter from Ceylon-SDK classes to the Java Reader API (which Java STaX requires).
 This will almost certainly be much slower than the Java SDK's implementations,
 unfortunately."
shared class CeylonReader(Reader wrapped) extends JReader() {
	shared actual overloaded Integer read() {
		if (exists retval = wrapped.readByte()) {
			return retval.unsigned;
		} else {
			return -1;
		}
	}
	shared actual void close() {}
	shared actual overloaded Integer read(CharArray cbuf, Integer offset, Integer length) {
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
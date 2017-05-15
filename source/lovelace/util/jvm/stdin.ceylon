import java.lang {
    JSystem=System
}
import java.io {
    FilterInputStream
}
"A wrapper around System.in to prevent it being closed."
shared object systemIn extends FilterInputStream(JSystem.\iin) {
    "Do *not* close."
    shared actual void close() {}
}
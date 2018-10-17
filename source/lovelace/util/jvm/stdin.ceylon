import java.lang {
    JSystem=System
}
import java.io {
    FilterInputStream
}
import lovelace.util.common {
    todo
}

"A wrapper around [[System.in|JSystem.in]] to prevent it being closed."
todo("Remove this; uses of the Java standard-stream objects are vanishingly
      rare in idiomatic Ceylon code.")
shared object systemIn extends FilterInputStream(JSystem.\iin) {
    "Do *not* close."
    shared actual void close() {}
}

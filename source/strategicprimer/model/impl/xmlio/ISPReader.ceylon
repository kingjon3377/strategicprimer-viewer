import java.io {
    JReader=Reader
}
import lovelace.util.common {
    todo,
    PathWrapper
}

import strategicprimer.model.common.xmlio {
    Warning
}

"An interface for readers of any SP model type."
shared interface ISPReader {
    "Read an object from XML."
    shared formal Element readXML<out Element=Object>(
            "The name of the file being read from"
            PathWrapper file,
            "The reader from which to read the XML"
            todo("Port to `ceylon.io` or `ceylon.buffer`")
            JReader istream,
            "The Warning instance to use for warnings"
            Warning warner) given Element satisfies Object;
}

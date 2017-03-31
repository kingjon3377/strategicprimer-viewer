import java.io {
    JReader=Reader
}
import java.nio.file {
    JPath=Path
}

import lovelace.util.common {
    todo
}

"An interface for readers of any SP model type."
shared interface ISPReader {
	"Read an object from XML."
	shared formal Element readXML<out Element>(
			"The name of the file being read from"
			todo("Port to `ceylon.file`")
			JPath file,
			"The reader from which to read the XML"
			todo("Port to `ceylon.io`")
			JReader istream,
			"The Warning instance to use for warnings"
			Warning warner) given Element satisfies Object;
}
"Tags we expect to use in the future; they are skipped for now and we'll warn if they're
 used."
shared {String*} futureTags = {"future", "explorer", "building", "resource", "changeset",
	"change", "move", "work", "discover", "submap", "futuretag", "futureTag"};
"The namespace that SP XML will use."
shared String spNamespace = "https://github.com/kingjon3377/strategicprimer-viewer";
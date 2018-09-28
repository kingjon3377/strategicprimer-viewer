import ceylon.collection {
    LinkedList,
    Stack
}
import java.io {
    FileNotFoundException,
    JCloseable=Closeable
}
import java.nio.file {
    JPath=Path
}
import java.util {
    NoSuchElementException
}

import javax.xml {
    XMLConstants
}
import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamException
}
import javax.xml.stream.events {
    XMLEvent,
    StartElement
}

import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    TypesafeXMLEventReader
}

import strategicprimer.model.common.xmlio {
    SPFormatException,
    spNamespace
}
import strategicprimer.model.impl.xmlio.exceptions {
    MissingPropertyException
}
import java.lang {
    AutoCloseable
}
"An extension to the [[Iterator]] of [[XMLEvent]] to automatically handle
 `include` tags."
todo("Use `ceylon.file`")
shared class IncludingIterator satisfies Iterator<XMLEvent> {
    "Get the 'file' attribute for the given tag."
    static String getFileAttribute(StartElement element) {
        for (QName name in [ QName(spNamespace, "file"), QName("file") ]) {
            if (exists attr = element.getAttributeByName(name),
                    exists val = attr.\ivalue) {
                return val;
            }
        } else {
            throw MissingPropertyException(element, "file");
        }
    }
    "The stack of iterators we're working with."
    Stack<[String, Iterator<XMLEvent>]> stack =
            LinkedList<[String, Iterator<XMLEvent>]>();
    shared new (JPath file, Iterator<XMLEvent> iter) {
        stack.push([file.string, iter]);
    }
    "Completely unwind the stack. Should be called before throwing any exception
     to our callers."
    void exhaust(Throwable? cause) {
        variable Exception? first = null;
        while (exists [file, reader] = stack.pop()) {
            try {
                if (is Destroyable reader) {
                    reader.destroy(cause);
                } else if (is JCloseable reader) {
                    reader.close();
                } else if (is AutoCloseable reader) {
                    reader.close();
                } else if (is Obtainable reader) {
                    reader.release(cause);
                }
            } catch (Exception except) {
                if (exists cause) {
                    if (cause === except) {
                        // This shouldn't be possible but in practice occurs.
                    } else {
                        cause.addSuppressed(except);
                    }
                } else if (exists previous = first) {
                    previous.addSuppressed(except);
                } else {
                    first = except;
                }
            }
        }
        if (!cause exists, exists exception = first) {
            throw exception;
        }
    }
    """Handle an "include" tag by adding an iterator for the contents of the file it
       references to the top of the stack."""
    throws(`class FileNotFoundException`,
        "when file referenced by <include> does not exist")
    throws(`class XMLStreamException`,
        "on XML parsing error in parsing the <include> tag or opening the included file")
    throws(`class SPFormatException`, "on SP format problem in <include>")
    todo("Ensure that any thrown exceptions make clear that there's inclusion involved")
    void handleInclude(StartElement tag) {
        try {
            String file = getFileAttribute(tag);
            // FIXME: Reader (and thus the file it opens!) gets leaked if not finished
            stack.push([file, TypesafeXMLEventReader(magicReader(file))]);
        } catch (Exception except) {
            exhaust(except);
            throw except;
        }
    }
    """Get the next item in the topmost iterator. We always make sure that there *is* a
       next item in the topmost iterator. If the next item would be an "include" tag, we
        open the file it specifies and push an iterator of its elements onto the stack."""
    shared actual XMLEvent|Finished next() {
        try {
            while (exists top = stack.top) {
                XMLEvent|Finished retval = top.rest.first.next();
                if (is Finished retval) {
                    if (exists [oldFile, oldStream] = stack.pop()) {
                        if (is JCloseable oldStream) {
                            oldStream.close();
                        } else if (is AutoCloseable oldStream) {
                            oldStream.close();
                        } else if (is Destroyable oldStream) {
                            oldStream.destroy(null);
                        } else if (is Obtainable oldStream) {
                            oldStream.release(null);
                        }
                    }
                    continue;
                } else if (is StartElement retval, [spNamespace, XMLConstants.nullNsUri]
                            .contains(retval.name.namespaceURI),
                        "include" == retval.name.localPart.lowercased) {
                    handleInclude(retval);
                    continue;
                } else {
                    return retval;
                }
            }
            return finished;
        } catch (Exception exception) {
            exhaust(exception);
            throw exception;
        }
    }
    "Get the file we're currently reading from."
    todo("Tests")
    shared String file {
        if (exists top = stack.top) {
            return top.first;
        } else {
            throw NoSuchElementException("We're not reading at all");
        }
    }
    shared actual String string {
        if (exists top = stack.top) {
            return "IncludingIterator, currently on ``top.first``";
        } else {
            return "Empty IncludingIterator";
        }
    }
}

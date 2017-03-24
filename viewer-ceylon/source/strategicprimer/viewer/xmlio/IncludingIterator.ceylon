import ceylon.collection {
    LinkedList,
    Stack
}

import controller.map.formatexceptions {
    MissingPropertyException,
    SPFormatException
}
import controller.map.iointerfaces {
    ISPReader
}
import controller.map.misc {
    MagicReader
}

import java.io {
    FileNotFoundException
}
import java.nio.file {
    JPath=Path
}
import java.util {
    JIterator=Iterator,
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
"An extension to the [[Iterator|JIterator]] of [[XMLEvent]] to automatically handle
 `include` tags."
todo("Satisfy Ceylon Iterator instead of Java Iterator",
    "Use `ceylon.file`")
shared class IncludingIterator satisfies JIterator<XMLEvent> {
    "Get the 'file' attribute for the given tag."
    static String getFileAttribute(StartElement element) {
        for (QName name in { QName(ISPReader.namespace, "file"), QName("file") }) {
            if (exists attr = element.getAttributeByName(name),
                    exists val = attr.\ivalue) {
                return val;
            }
        } else {
            throw MissingPropertyException(element, "file");
        }
    }
    "The stack of iterators we're working with."
    Stack<[String, JIterator<XMLEvent>]> stack =
            LinkedList<[String, JIterator<XMLEvent>]>();
    shared new (JPath file, JIterator<XMLEvent> iter) {
        stack.push([file.string, iter]);
    }
    "Remove any empty iterators from the top of the stack."
    void removeEmptyIterators() {
        while (exists top = stack.top, !top.rest.first.hasNext()) {
            stack.pop();
        }
    }
    "Return whether there are any events left. Note that this method removes any empty
     iterators from the top of the stack before returning."
    shared actual Boolean hasNext() {
        removeEmptyIterators();
        return stack.top exists;
    }
    """Handle an "include" tag by adding an iterator for the contents of the file it
       references to the top of the stack."""
    void handleInclude(StartElement tag) {
        try {
            String file = getFileAttribute(tag);
            // FIXME: The MagicReader here (and thus the file it opens!) get leaked!
            stack.push([file, TypesafeXMLEventReader(MagicReader(file))]);
        } catch (FileNotFoundException except) {
            throw NoSuchElementBecauseException("File referenced by <include> not found",
                except);
        } catch (XMLStreamException except) {
            // TODO: Tests should handle include-non-XML case
            throw NoSuchElementBecauseException(
                "XML stream error parsing <include> tag or opening file", except);
        } catch (SPFormatException except) {
            throw NoSuchElementBecauseException("SP format problem in <include>", except);
        }
    }
    """Get the next item in the topmost iterator. We always make sure that there *is* a
       next item in the topmost iterator. If the next item would be an "include" tag, we
        open the file it specifies and push an iterator of its elements onto the stack. On
        error in that process, we throw a NoSuchElementException, as that's the only
        checked exception the Java version could throw."""
    shared actual XMLEvent next() {
        removeEmptyIterators();
        if (exists top = stack.top) {
            variable XMLEvent retval = top.rest.first.next();
            while (is StartElement temp = retval,
                {ISPReader.namespace, XMLConstants.nullNsUri}
                    .contains(temp.name.namespaceURI), "include" == temp.name.localPart) {
                handleInclude(temp);
                removeEmptyIterators();
                if (exists tempTwo = stack.top) {
                    retval = tempTwo.rest.first.next();
                } else {
                    throw NoSuchElementException();
                }
            }
            return retval;
        } else {
            throw NoSuchElementException();
        }
    }
    "Remove the next item from the topmost iterator in the stack; ensure that no empty
     iterator is on the top of the stack both before and after doing so."
    shared actual void remove() {
        removeEmptyIterators();
        if (exists top = stack.top) {
            top.rest.first.remove();
        } else {
            throw NoSuchElementException();
        }
        removeEmptyIterators();
    }
    "Get the file we're currently reading from."
    todo("Tests")
    shared String file {
        removeEmptyIterators();
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
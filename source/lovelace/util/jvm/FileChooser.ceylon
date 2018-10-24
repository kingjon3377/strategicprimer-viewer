import ceylon.language.meta.model {
    InvocationException
}
import java.lang.reflect {
    InvocationTargetException
}
import javax.swing {
    SwingUtilities,
    JFileChooser
}
import ceylon.language.meta {
    type
}
import java.io {
    JFile=File
}
import java.lang {
    InterruptedException
}
import java.awt {
    JFileDialog=FileDialog,
    Component
}
import ceylon.logging {
    Logger,
    logger
}
import lovelace.util.common {
    PathWrapper,
    todo
}

"Logger."
Logger log = logger(`module lovelace.util.jvm`);

"A wrapper around the [[Swing|JFileChooser]] and [[AWT|JFileDialog]] file-choosers.
 
 On most platforms, [[the Swing JFileChooser|JFileChooser]] is close enough to the
 native widget in appearance and functionality; on macOS, it is decidedly *not*,
 and it's impossible to conform to the platform HIG without using 
 [[the AWT FileDialog|JFileDialog]] class instead. This class leaves the choice of
 which one to use to its callers, but abstracts over the differences between them."
shared class FileChooser {
    "An exception to throw when the user cancels the file-chooser."
    shared static class ChoiceInterruptedException(Throwable? cause = null)
            extends Exception((cause exists)
                then "Choice of a file was interrupted by an exception:"
                else "No file was selected", cause) {}
    "Convert the type returned by the file-chooser to the type we expose in
     return types."
    todo("Once ceylon.file is marked as cross-platform, convert [[PathWrapper]] usage
          back to [[ceylon.file::Path]].")
    static PathWrapper fileToPath(JFile file) => PathWrapper(file.toPath().string);

    "The method to call to show the caller's chosen dialog."
    Integer(Component?) chooserFunction;
    "The file(s) either passed in to the constructor or chosen by the user."
    variable {PathWrapper+}? storedFile;
    "The file-chooser widget that will actually ask the user to choose a file or files."
    JFileChooser|JFileDialog chooser;

    """Constructor for what will be an "Open" dialog, allowing the user to choose multiple
       files."""
    shared new open(JFileChooser|JFileDialog fileChooser, PathWrapper? loc = null) {
        log.trace("FileChooser invoked for the Open dialog");
        switch (fileChooser)
        case (is JFileChooser) {
            log.trace("Using Swing JFileChooser");
            chooserFunction = fileChooser.showOpenDialog;
            fileChooser.multiSelectionEnabled = true;
        }
        case (is JFileDialog) {
            log.trace("Using AWT FileDialog");
            fileChooser.mode = JFileDialog.load;
            chooserFunction = (Component? component) {
                fileChooser.setVisible(true);
                return 0;
            };
            fileChooser.multipleMode = true;
        }
        if (exists loc) {
            log.trace("A file was passed in");
            storedFile = Singleton(loc);
        } else {
            log.trace("No file was passed in");
            storedFile = null;
        }
        chooser = fileChooser;
    }

    """Constructor for what will be a "Save" dialog."""
    shared new save(PathWrapper? loc,
            JFileChooser|JFileDialog fileChooser) {
        log.trace("FileChooser invoked for Save dialog");
        switch (fileChooser)
        case (is JFileChooser) {
            log.trace("Using Swing JFileChooser");
            chooserFunction = fileChooser.showSaveDialog;
        }
        case (is JFileDialog) {
            log.trace("Using AWT FileDialog");
            fileChooser.mode = JFileDialog.save;
            chooserFunction = (Component? component) {
                fileChooser.setVisible(true);
                return 0;
            };
        }
        if (exists loc) {
            log.trace("A file was passed in");
            storedFile = Singleton(loc);
        } else {
            log.trace("No file was passed in");
            storedFile = null;
        }
        chooser = fileChooser;
    }

    """Constructor for a "Custom" dialog. This feature only actually
       works with [[the Swing JFileChooser|JFileChooser]], so on AWT
       we fall back to a "Save" dialog."""
    shared new custom(PathWrapper? loc, String approveText,
            JFileChooser|JFileDialog fileChooser) {
        log.trace("FileChooser invoked for a custom dialog");
        switch (fileChooser)
        case (is JFileChooser) {
            log.trace("Using Swing JFileChooser");
            chooserFunction = (Component? component) =>
            	fileChooser.showDialog(component, approveText);
        }
        case (is JFileDialog) {
            log.trace("Using AWT FileDialog, specifying Save");
            // Unfortunately, it's not possible to use a 'custom' action with the AWT
            // interface.
            fileChooser.mode = JFileDialog.save;
            chooserFunction = (Component? component) {
                fileChooser.setVisible(true);
                return 0;
            };
        }
        if (exists loc) {
            log.trace("A file was passed in");
            storedFile = Singleton(loc);
        } else {
            log.trace("No file was passed in");
            storedFile = null;
        }
        chooser = fileChooser;
    }

    "A helper method to run a function on the EDT, without leaking any of the
     implementation-detail exceptions that are commonly thrown to the caller."
    void invoke(Anything() runnable) {
        try {
            log.trace("FileChooser.invoke(): About to invoke the provided function");
            SwingUtilities.invokeAndWait(runnable);
        } catch (InvocationTargetException|InvocationException except) {
            if (exists cause = except.cause) {
                throw ChoiceInterruptedException(cause);
            } else {
                throw ChoiceInterruptedException(except);
            }
        } catch (InterruptedException except) {
            throw ChoiceInterruptedException(except);
        }
    }

    "Show the dialog to the user and update [[storedFile]] with his or her choice(s)."
    void haveUserChooseFiles() {
        log.trace("In FileChooser.haveUserChooseFiles");
        Integer status = chooserFunction(null);
        log.trace("FileChooser: The AWT or Swing chooser returned");
        if (is JFileChooser chooser) {
            if (status == JFileChooser.approveOption) {
                value retval = chooser.selectedFiles.iterable.coalesced
                    .collect(fileToPath);
                if (nonempty retval) {
                    log.trace("Saving the file(s) the user chose via Swing");
                    storedFile = retval;
                } else {
                    log.info("User pressed approve but selected no files");
                    storedFile = null;
                }
            } else {
                log.info("Chooser function returned ``status``");
            }
        } else {
            value retval = chooser.files.iterable.coalesced.collect(fileToPath);
            if (nonempty retval) {
                log.trace("Saving the file(s) the user chose via AWT");
                storedFile = retval;
            } else {
                log.debug("User failed to choose?");
                log.debug("Returned iterable was ``retval`` (``type(retval)``");
                storedFile = null;
            }
        }
    }

    "If a valid filename was, or multiple filenames were, passed in to the constructor,
     return an iterable containing it or them; otherwise, show a dialog for the user to
     select one or more filenames and return the filename(s) the user selected. Throws an
     exception if the choice is interrupted or the user declines to choose."
    throws(`class ChoiceInterruptedException`)
    shared {PathWrapper+} files {
        if (exists temp = storedFile) {
            log.trace("FileChooser.files: A file was stored, so returning it");
            return temp;
        } else if (SwingUtilities.eventDispatchThread) {
            log.trace("FileChooser.files: Have to ask the user; on EDT");
            haveUserChooseFiles();
        } else {
            log.trace("FileChooser.files: Have to ask the user; not yet on EDT");
            invoke(haveUserChooseFiles);
        }
        if (exists temp = storedFile) {
            return temp;
        } else {
            throw ChoiceInterruptedException();
        }
    }
    "Set the stored file(s) to the given Iterable."
    assign files {
        storedFile = files;
    }

    "Allow the user to choose a file or files, if necessary, and pass each file to the
     given consumer. If the operation is canceled, do nothing."
    shared void call(Anything(PathWrapper) consumer) {
        try {
            files.each(consumer);
        } catch (ChoiceInterruptedException exception) {
            log.info("Choice interrupted or user failed to choose", exception);
        }
    }
}

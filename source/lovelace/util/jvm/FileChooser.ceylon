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
import ceylon.file {
    parsePath,
    Path
}

"Logger."
Logger log = logger(`module lovelace.util.jvm`);

"A wrapper around the Swing and AWT file-choosers."
shared class FileChooser {
    shared static class ChoiceInterruptedException(Throwable? cause = null)
            extends Exception((cause exists)
                then "Choice of a file was interrupted by an exception:"
                else "No file was selected", cause) {}
    static Path fileToPath(JFile file) => parsePath(file.toPath().string);
    Integer(Component?) chooserFunction;
    variable {Path+}? storedFile;
    JFileChooser|JFileDialog chooser;
    shared new open(JFileChooser|JFileDialog fileChooser, Path? loc = null) {
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
    shared new save(Path? loc,
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
    shared new custom(Path? loc, String approveText,
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
            }
        }
    }
    "If a valid filename was, or multiple filenames were, passed in to the constructor,
     return an iterable containing it or them; otherwise, show a dialog for the user to
     select one or more filenames and return the filename(s) the user selected. Throws an
     exception if the choice is interrupted or the user declines to choose."
    shared {Path+} files {
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
    assign files {
        storedFile = files;
    }
    "Allow the user to choose a file or files, if necessary, and pass each file to the
     given consumer. If the operation is canceled, do nothing."
    shared void call(Anything(Path) consumer) {
        try {
            files.each(consumer);
        } catch (ChoiceInterruptedException exception) {
            log.info("Choice interrupted or user failed to choose", exception);
        }
    }
}
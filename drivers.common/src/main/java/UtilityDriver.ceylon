"""An interface to allow utility drivers, which operate on files rather than a map model,
   to be a "functional" (single-method-to-implement) interface"""
shared interface UtilityDriver satisfies ISPDriver {
    "Run the driver. If the driver is a GUI driver, this should use
     SwingUtilities.invokeLater(); if it's a CLI driver, that's not necessary."
    shared formal void startDriver(
            "Any command-line arguments, such as filenames, that should be passed to the
             driver. This will not include options."
            String* args);
}

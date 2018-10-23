
import strategicprimer.model.impl.xmlio {
    ISPReader,
    SPWriter
}
import strategicprimer.model.impl.xmlio.fluidxml {
    SPFluidReader,
    SPFluidWriter
}
import strategicprimer.model.impl.xmlio.yaxml {
    yaXMLReader,
    yaXMLWriter
}

"A factory to produce instances of the current and old map readers, to test against. (So
 we don't have to ignore *all* deprecation warnings in the test class, and we only have to
  change one place when we create a new implementation.)"
shared object testReaderFactory {
    """The "new" (currently-used-by-default) reader implementation. (Actually
       older than [[oldReader]], but we switched back because this is the
       faster implementation."""
    shared ISPReader&IMapReader newReader => SPFluidReader();

    """The "old" reader implementation."""
    shared ISPReader&IMapReader oldReader => yaXMLReader;

    "The older writer implementation."
    shared SPWriter oldWriter => SPFluidWriter();

    "The newer (currently-used-by-default) writer implementation."
    shared SPWriter newWriter => yaXMLWriter;
}

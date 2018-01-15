
import strategicprimer.model.xmlio {
    ISPReader,
    SPWriter
}
import strategicprimer.model.xmlio.fluidxml {
    SPFluidReader,
    SPFluidWriter
}
import strategicprimer.model.xmlio.yaxml {
    yaXMLReader,
    yaXMLWriter
}
"A factory to produce instances of the current and old map readers, to test against. (So
 we don't have to ignore *all* deprecation warnings in the test class, and we only have to
  change one place when we create a new implementation.)"
shared object testReaderFactory {
    shared ISPReader&IMapReader newReader => SPFluidReader();
    shared ISPReader&IMapReader oldReader => yaXMLReader;
    shared SPWriter oldWriter => SPFluidWriter();
    shared SPWriter newWriter => yaXMLWriter;
}


import strategicprimer.viewer.xmlio.fluidxml {
    SPFluidReader,
    SPFluidWriter
}
import strategicprimer.viewer.xmlio.yaxml {
    yaXMLReader,
    yaXMLWriter
}
"A factory to produce instances of the current and old map readers, to test against. (So
 we don't have to ignore *all* deprecation warnings in the test class, and we only have to
  change one place when we create a new implementation.)"
shared object testReaderFactory {
    shared ISPReader oldReader => SPFluidReader();
    shared ISPReader newReader => yaXMLReader;
    shared SPWriter oldWriter => SPFluidWriter();
    shared SPWriter newWriter => yaXMLWriter;
}
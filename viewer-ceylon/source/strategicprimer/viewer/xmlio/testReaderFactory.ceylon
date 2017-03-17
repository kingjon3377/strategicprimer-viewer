import controller.map.iointerfaces {
    ISPReader,
    SPWriter
}
import controller.map.yaxml {
    YAXMLReader,
    YAXMLWriter
}
import strategicprimer.viewer.xmlio.fluidxml {
    SPFluidReader,
    SPFluidWriter
}
"A factory to produce instances of the current and old map readers, to test against. (So
 we don't have to ignore *all* deprecation warnings in the test class, and we only have to
  change one place when we create a new implementation.)"
shared object testReaderFactory {
    shared ISPReader oldReader => SPFluidReader();
    shared ISPReader newReader => YAXMLReader();
    shared SPWriter oldWriter => SPFluidWriter();
    shared SPWriter newWriter => YAXMLWriter();
}
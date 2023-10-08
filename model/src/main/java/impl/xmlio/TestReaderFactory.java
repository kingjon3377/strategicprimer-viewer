package impl.xmlio;

import impl.xmlio.fluidxml.SPFluidReader;
import impl.xmlio.fluidxml.SPFluidWriter;
import impl.xmlio.yaxml.YAXMLReader;
import impl.xmlio.yaxml.YAXMLWriter;

/**
 * A factory to produce instances of the current and old map readers, to test
 * against. (So we don't have to ignore *all* deprecation warnings in the test
 * class, and we only have to change one place when we create a new implementation.)
 */
public final class TestReaderFactory {
    private TestReaderFactory() {
    }

    /**
     * The "new" (currently-used-by-default) reader implementation.
     * (Actually older than {@link #getOldSPReader}, but we switched back because
     * this is the faster implementation.
     */
    public static ISPReader getNewSPReader() {
        return new SPFluidReader();
    }

    /**
     * The "new" (currently-used-by-default) reader implementation.
     * (Actually older than {@link #getOldMapReader()}, but we switched back because
     * this is the faster implementation.
     */
    public static IMapReader getNewMapReader() {
        return new SPFluidReader();
    }

    /**
     * The "old" reader implementation.
     */
    public static ISPReader getOldSPReader() {
        return new YAXMLReader();
    }

    /**
     * The "old" reader implementation.
     */
    public static IMapReader getOldMapReader() {
        return new YAXMLReader();
    }

    /**
     * The older writer implementation.
     */
    public static SPWriter getOldWriter() {
        return new SPFluidWriter();
    }

    /**
     * The newer (currently-used-by-default) writer implementation.
     */
    public static SPWriter getNewWriter() {
        return new YAXMLWriter();
    }
}

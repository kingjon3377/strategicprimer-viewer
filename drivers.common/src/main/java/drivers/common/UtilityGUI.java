package drivers.common;

import java.nio.file.Path;

/**
 * An interface for utility GUI apps that can respond to an "open" menu item.
 */
public interface UtilityGUI extends UtilityDriver {
    void open(Path path);
}

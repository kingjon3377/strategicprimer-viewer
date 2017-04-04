import java.awt {
    Dimension
}
import java.nio.file {
    JPath=Path
}

import javax.swing {
    JFrame,
    WindowConstants
}
"An intermediate subclass of JFrame to take care of some common setup things that can't be
 done in an interface."
shared abstract class SPFrame(String windowTitle, JPath? file, Dimension? minSize = null)
		extends JFrame(windowTitle) satisfies ISPWindow {
	if (exists file) {
		title = "``file`` | ``windowTitle``";
		rootPane.putClientProperty("Window.documentFile", file.toFile());
	}
	defaultCloseOperation = WindowConstants.disposeOnClose;
	if (exists minSize) {
		setMinimumSize(minSize);
	}
}

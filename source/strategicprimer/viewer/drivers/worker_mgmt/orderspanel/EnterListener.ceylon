import java.awt.event {
    KeyAdapter,
    KeyEvent
}
import lovelace.util.jvm {
    platform
}
class EnterListener(Anything() delegate) extends KeyAdapter() {
    shared actual void keyPressed(KeyEvent event) {
        if (event.keyCode == KeyEvent.vkEnter, platform.hotKeyPressed(event)) {
            delegate();
        }
    }
}

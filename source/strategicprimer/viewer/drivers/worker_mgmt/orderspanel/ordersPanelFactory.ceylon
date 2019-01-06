import javax.swing {
    JScrollPane,
    JSpinner,
    SpinnerNumberModel,
    JLabel,
    JTextArea,
    JPanel,
    JButton,
    JComponent,
    KeyStroke
}
import lovelace.util.jvm {
    BorderedPanel,
    centeredHorizontalBox,
    platform,
    listenedButton,
    createHotKey
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.common.map {
    Player
}
import java.awt.event {
    KeyAdapter,
    KeyEvent
}
import lovelace.util.common {
    silentListener
}
"Create and return a panel for the user to enter a unit's orders or read a unit's
 results."
shared JPanel&OrdersContainer ordersPanel(Integer currentTurn, Player currentPlayer,
        {IUnit*}(Player, String) playerUnits,
        String(IUnit, Integer) ordersSupplier,
        Anything(IUnit, Integer, String)? ordersConsumer,
        Anything() modificationListener) {
    JTextArea area = JTextArea();

    Integer minimumTurn = (currentTurn<0) then currentTurn else - 1;
    Integer maximumTurn = (currentTurn>100) then currentTurn else 100;
    SpinnerNumberModel spinnerModel = SpinnerNumberModel(currentTurn, minimumTurn,
        maximumTurn, 1);

    value retval = OrdersPanel(currentTurn, currentPlayer, playerUnits, ordersSupplier,
        ordersConsumer, modificationListener, spinnerModel, area);

    value revertListener = silentListener(retval.revert);

    if (exists ordersConsumer) {
        JButton applyButton = listenedButton("Apply", silentListener(retval.apply));
        JButton revertButton = listenedButton("Revert", revertListener);
        platform.makeButtonsSegmented(applyButton, revertButton);

        JPanel buttonPanel = (platform.systemIsMac) then
            centeredHorizontalBox(revertButton, applyButton)
            else BorderedPanel.horizontalPanel(revertButton, null, applyButton);
        String prefix = platform.shortcutDescription;
        retval.pageStart = BorderedPanel.horizontalPanel(
            JLabel("Orders for current selection, if a unit: (``prefix``D)"), null,
            BorderedPanel.horizontalPanel(null, JLabel("Turn "),
                JSpinner(spinnerModel)));
        retval.pageEnd = buttonPanel;
    } else {
        retval.pageStart = BorderedPanel.horizontalPanel(
            JLabel("Results for current selection, if a unit"), null,
            BorderedPanel.horizontalPanel(null, JLabel("Turn "),
                JSpinner(spinnerModel)));
    }
    retval.center = JScrollPane(area);
    area.lineWrap = true;
    area.wrapStyleWord = true;
    spinnerModel.addChangeListener(revertListener);

    object modifiedEnterListener extends KeyAdapter() {
        shared actual void keyPressed(KeyEvent event) {
            if (event.keyCode == KeyEvent.vkEnter, platform.hotKeyPressed(event)) {
                retval.apply();
            }
        }
    }
    area.addKeyListener(modifiedEnterListener);

    Integer keyMask = platform.shortcutMask;
    createHotKey(retval, "openOrders", (event) { // TODO: Convert to method on OrdersPanel?
        Boolean newlyGainingFocus = !area.focusOwner;
        area.requestFocusInWindow();
        if (newlyGainingFocus) {
            area.selectAll();
        }
    }, JComponent.whenInFocusedWindow, KeyStroke.getKeyStroke(KeyEvent.vkD, keyMask)); // TODO: Use createAccelerator()
    return retval;
}

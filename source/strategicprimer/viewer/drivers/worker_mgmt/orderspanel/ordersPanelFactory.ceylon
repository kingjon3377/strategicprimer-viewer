import javax.swing {
    JScrollPane,
    JSpinner,
    SpinnerNumberModel,
    JLabel,
    JTextArea,
    JPanel,
    JButton
}
import lovelace.util.jvm {
    BorderedPanel,
    centeredHorizontalBox,
    platform,
    ListenedButton
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.common.map {
    Player
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

    if (exists ordersConsumer) {
        JButton applyButton = ListenedButton("Apply", retval.apply);
        JButton revertButton = ListenedButton("Revert", retval.revert);
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

    return retval;
}

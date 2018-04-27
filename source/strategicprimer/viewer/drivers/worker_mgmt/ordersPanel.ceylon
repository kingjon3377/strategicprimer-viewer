import java.awt.event {
    KeyEvent,
    KeyAdapter
}

import javax.swing {
    SpinnerNumberModel,
    KeyStroke,
    JButton,
    JPanel,
    JLabel,
    JSpinner,
    JScrollPane,
    JTextArea,
    JComponent
}
import javax.swing.event {
    TreeSelectionListener,
    TreeSelectionEvent
}
import javax.swing.tree {
    DefaultMutableTreeNode
}

import lovelace.util.jvm {
    platform,
    centeredHorizontalBox,
    listenedButton,
    createHotKey,
    BorderedPanel
}

import strategicprimer.drivers.common {
    PlayerChangeListener
}
import strategicprimer.model.map {
    Player
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    ProxyUnit
}
import lovelace.util.common {
	silentListener
}
"A panel for the user to enter a unit's orders or read a unit's results."
JPanel&Applyable&Revertible&TreeSelectionListener&PlayerChangeListener ordersPanel(
        Integer currentTurn, variable Player currentPlayer,
        {IUnit*}(Player, String) playerUnits,
        String(IUnit, Integer) ordersSupplier,
        Anything(IUnit, Integer, String)? ordersConsumer) {
    JTextArea area = JTextArea();
    Integer minimumTurn = (currentTurn<0) then currentTurn else - 1;
    Integer maximumTurn = (currentTurn>100) then currentTurn else 100;
    SpinnerNumberModel spinnerModel = SpinnerNumberModel(currentTurn, minimumTurn,
        maximumTurn, 1);
    object retval extends BorderedPanel()
            satisfies Applyable&Revertible&TreeSelectionListener&PlayerChangeListener {
        variable Anything selection = null;
        "If a unit is selected, change its orders to what the user wrote."
        shared actual void apply() {
            if (is IUnit sel = selection) {
                if (exists ordersConsumer) {
                    ordersConsumer(sel, spinnerModel.number.intValue(), area.text);
                }
                parent.parent.repaint();
            }
        }
        "Change the text in the area to either the current orders, if a unit is selected,
          or the empty string, if one is not."
        shared actual void revert() {
            if (is IUnit sel = selection) {
                area.text = ordersSupplier(sel, spinnerModel.number.intValue());
            } else {
                area.text = "";
            }
        }
        "Handle a changed value in the tree."
        shared actual void valueChanged(TreeSelectionEvent event) {
            if (exists selectedPath = event.newLeadSelectionPath) {
                value sel = selectedPath.lastPathComponent;
                Object temp;
                if (is DefaultMutableTreeNode sel) {
                    temp = sel.userObject;
                } else {
                    temp = sel;
                }
                if (is String temp) {
                    ProxyUnit proxyUnit = ProxyUnit.forKind(temp);
                    for (unit in playerUnits(currentPlayer, temp)) {
                        proxyUnit.addProxied(unit);
                    }
                    selection = proxyUnit;
                } else {
                    selection = temp;
                }
                revert();
            }
        }
        shared actual void playerChanged(Player? old, Player newPlayer) =>
                currentPlayer = newPlayer;
    }
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
    createHotKey(retval, "openOrders", (event) {
        Boolean newlyGainingFocus = !area.focusOwner;
        area.requestFocusInWindow();
        if (newlyGainingFocus) {
            area.selectAll();
        }
    }, JComponent.whenInFocusedWindow, KeyStroke.getKeyStroke(KeyEvent.vkD, keyMask));
    return retval;
}

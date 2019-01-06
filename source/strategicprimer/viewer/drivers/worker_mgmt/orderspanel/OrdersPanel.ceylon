import lovelace.util.jvm {
    BorderedPanel,
    createHotKey,
    platform
}
import strategicprimer.model.common.map {
    Player
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    ProxyUnit
}
import javax.swing {
    SpinnerNumberModel,
    JTextArea,
    JComponent,
    KeyStroke
}
import javax.swing.event {
    TreeSelectionEvent
}
import javax.swing.tree {
    DefaultMutableTreeNode
}
import lovelace.util.common {
    silentListener
}
import java.awt.event {
    KeyEvent
}
class OrdersPanel(Integer currentTurn, variable Player currentPlayer,
            {IUnit*}(Player, String) playerUnits,
            String(IUnit, Integer) ordersSupplier,
            Anything(IUnit, Integer, String)? ordersConsumer,
            Anything() modificationListener, SpinnerNumberModel spinnerModel,
            JTextArea area)
        extends BorderedPanel() satisfies OrdersContainer {
    variable Anything selection = null;
    "If a unit is selected, change its orders to what the user wrote."
    shared actual void apply() {
        if (is IUnit sel = selection) {
            if (exists ordersConsumer) {
                ordersConsumer(sel, spinnerModel.number.intValue(), area.text);
                modificationListener();
            }
            parent.parent.repaint();
        }
    }

    "Change the text in the area to either the current orders, if a unit is selected,
      or the empty string, if one is not."
    shared actual void revert() {
        if (is IUnit sel = selection) {
            area.enabled = true;
            area.text = ordersSupplier(sel, spinnerModel.number.intValue());
        } else {
            area.enabled = false;
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
                playerUnits(currentPlayer, temp).each(proxyUnit.addProxied);
                selection = proxyUnit;
            } else {
                selection = temp;
            }
            revert();
        }
    }

    shared actual void playerChanged(Player? old, Player newPlayer) =>
        currentPlayer = newPlayer;

    shared void focusOnArea() {
        Boolean newlyGainingFocus = !area.focusOwner;
        area.requestFocusInWindow();
        if (newlyGainingFocus) {
            area.selectAll();
        }
    }

    spinnerModel.addChangeListener(silentListener(revert));

    area.addKeyListener(EnterListener(apply));

    createHotKey(area, "openOrders", silentListener(focusOnArea),
        JComponent.whenInFocusedWindow,
        KeyStroke.getKeyStroke(KeyEvent.vkD, platform.shortcutMask));
}

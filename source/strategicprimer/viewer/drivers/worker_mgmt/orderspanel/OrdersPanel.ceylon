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

import java.awt {
    Color
}

final class OrdersPanel extends BorderedPanel satisfies OrdersContainer {
    static Color lightBlue = Color(135, 206, 250);
    variable Player currentPlayer;
    {IUnit*}(Player, String) playerUnits;
    String(IUnit, Integer) ordersSupplier;
    Anything(IUnit, Integer, String)? ordersConsumer;
    Boolean(IUnit, Integer) isCurrent;
    Anything() modificationListener;
    SpinnerNumberModel spinnerModel;
    Color defaultColor;
    JTextArea area;
    shared new (Integer currentTurn, Player currentPlayer,
            {IUnit*}(Player, String) playerUnits, String(IUnit, Integer) ordersSupplier,
            Anything(IUnit, Integer, String)? ordersConsumer,
            Boolean(IUnit, Integer) isCurrent,
            Anything() modificationListener, SpinnerNumberModel spinnerModel,
            JTextArea area) extends BorderedPanel() {
        this.currentPlayer = currentPlayer;
        this.playerUnits = playerUnits;
        this.ordersSupplier = ordersSupplier;
        this.ordersConsumer = ordersConsumer;
        this.isCurrent = isCurrent;
        this.modificationListener = modificationListener;
        this.spinnerModel = spinnerModel;
        this.area = area;
        defaultColor = area.background;
    }

    variable Anything selection = null;

    void fixColor() {
        if (is IUnit sel = selection, !isCurrent(sel, spinnerModel.number.intValue())) {
            area.background = lightBlue;
        } else {
            area.background = defaultColor;
        }
    }

    "If a unit is selected, change its orders to what the user wrote."
    shared actual void apply() {
        if (is IUnit sel = selection) {
            if (exists ordersConsumer) {
                ordersConsumer(sel, spinnerModel.number.intValue(), area.text);
                modificationListener();
            }
            fixColor();
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
        fixColor();
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
        fixColor();
        if (newlyGainingFocus) {
            area.selectAll();
        }
    }

    shared actual Boolean selectText(String selection) {
        String text = area.text.lowercased;
        Integer index = text.indexOf(selection.lowercased);
        if (index.negative) {
            return false;
        } else {
            area.requestFocusInWindow();
            area.caretPosition = index;
            area.moveCaretPosition(index + selection.size);
            return true;
        }
    }

    spinnerModel.addChangeListener(silentListener(revert));

    area.addKeyListener(EnterListener(apply));

    createHotKey(area, "openOrders", silentListener(focusOnArea),
        JComponent.whenInFocusedWindow,
        KeyStroke.getKeyStroke(KeyEvent.vkD, platform.shortcutMask));
}

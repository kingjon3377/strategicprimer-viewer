import strategicprimer.viewer.drivers.worker_mgmt {
    Applyable,
    Revertible
}
import javax.swing.event {
    TreeSelectionListener
}
import strategicprimer.drivers.common {
    PlayerChangeListener
}
shared interface OrdersContainer
        satisfies Applyable&Revertible&TreeSelectionListener&PlayerChangeListener {
    "If the given string is present (ignoring case) in the orders or results text area,
     cause that string to become selected in the text and return true; otherwise, return
     false."
    shared formal Boolean selectText(String substring);
}

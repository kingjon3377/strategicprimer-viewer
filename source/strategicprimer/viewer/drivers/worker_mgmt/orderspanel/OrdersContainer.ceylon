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
}

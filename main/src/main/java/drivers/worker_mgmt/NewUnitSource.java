package drivers.worker_mgmt;

import worker.common.NewUnitListener;

/**
 * An interface for {@link NewUnitListener}s to listen to.
 */
public interface NewUnitSource {
    /**
     * Add a listener.
     */
    void addNewUnitListener(NewUnitListener listener);

    /**
     * Remove a listener.
     */
    void removeNewUnitListener(NewUnitListener listener);
}

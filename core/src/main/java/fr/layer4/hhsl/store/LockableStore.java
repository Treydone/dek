package fr.layer4.hhsl.store;

public interface LockableStore extends Store {

    /**
     * Initialize a clear or a secured store.
     *
     * @param secured
     */
    void init(boolean secured);

    void changePassword();

    /**
     * Unlock a previously secured store by prompting for the password.
     */
    void unlock();

    boolean isUnlocked();
}

package fr.layer4.hhsl.local;

public interface LocalStore {

    void init(boolean secured);

    void purge();

    void changePassword();

    void unlock();

    boolean isReady();

    boolean isUnlocked();
}

package fr.layer4.hhsl;

import fr.layer4.hhsl.store.LockableStore;
import fr.layer4.hhsl.store.Store;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;

public class UnlockablePromptProvider implements PromptProvider {

    @Autowired
    private Store store;

    @Override
    public AttributedString getPrompt() {
        if (store instanceof LockableStore) {
            LockableStore lockableStore = (LockableStore) this.store;
            if (lockableStore.isUnlocked()) {
                return new AttributedString("hhsl:>",
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            } else {
                return new AttributedString("locked:>",
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            }
        } else {
            return new AttributedString("hhsl:>",
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
        }
    }
}

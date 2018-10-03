package fr.layer4.hhsl;

import fr.layer4.hhsl.local.LocalStore;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;

public class UnlockablePromptProvider implements PromptProvider {

    @Autowired
    private LocalStore localStore;

    @Override
    public AttributedString getPrompt() {
        if (localStore.isUnlocked()) {
            return new AttributedString("hhsl:>",
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
        } else {
            return new AttributedString("locked:>",
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
        }
    }
}

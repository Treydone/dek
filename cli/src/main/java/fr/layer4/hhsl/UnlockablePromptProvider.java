package fr.layer4.hhsl;

/*-
 * #%L
 * HHSL
 * %%
 * Copyright (C) 2018 Layer4
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import fr.layer4.hhsl.store.LockableStore;
import fr.layer4.hhsl.store.Store;
import lombok.RequiredArgsConstructor;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UnlockablePromptProvider implements PromptProvider {

    @Autowired
    private final Store store;

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

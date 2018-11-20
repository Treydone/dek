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
package fr.layer4.hhsl.prompt;

import org.jline.reader.LineReader;
import org.jline.reader.MaskingCallback;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class JLinePrompter implements Prompter {

    public static final String YES = "Y";
    public static final String NO = "n";
    @Autowired
    @Lazy
    private Terminal terminal;

    @Autowired
    @Lazy
    private LineReader lineReader;

    @Autowired
    private MaskingCallback maskingCallback;

    public String doublePromptForPassword() {
        String line1 = promptForPassword("password for root: ");
        String line2 = promptForPassword("re-type password for root: ");

        if (!line1.equals(line2)) {
            throw new RuntimeException("Password did not match");
        }
        return line1;
    }

    public String promptForRootPassword() {
        return promptForPassword("password for root: ");
    }

    public String promptForPassword(String message) {
        return lineReader.readLine(message, null, maskingCallback, null);
    }

    public String prompt(String message) {
        return lineReader.readLine(message);
    }

    public boolean promptForQuestion(String message) {
        String prompt = null;
        while (!NO.equals(prompt) && !YES.equals(prompt)) {
            if (prompt != null) {
                this.terminal.writer().println("Please choose " + YES + " or " + NO);
            }
            prompt = prompt(message);
        }
        return !prompt.equals(NO);
    }
}

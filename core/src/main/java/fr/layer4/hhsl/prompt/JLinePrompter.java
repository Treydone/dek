package fr.layer4.hhsl.prompt;

import org.jline.reader.LineReader;
import org.jline.reader.MaskingCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class JLinePrompter implements Prompter {

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
}

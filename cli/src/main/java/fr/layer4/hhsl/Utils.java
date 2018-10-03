package fr.layer4.hhsl;

import org.jline.reader.LineReader;
import org.jline.reader.MaskingCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class Utils {

    @Autowired
    @Lazy
    private LineReader lineReader;

    @Autowired
    private MaskingCallback maskingCallback;

    public String doublePromptForPassword() {
        String line1 = lineReader.readLine("password for root: ", null, maskingCallback, null);
        String line2 = lineReader.readLine("re-type password for root: ", null, maskingCallback, null);

        if (!line1.equals(line2)) {
            throw new RuntimeException("Password did not match");
        }
        return line1;
    }

    public String promptForPassword() {
        return lineReader.readLine("password for root: ", null, maskingCallback, null);
    }
}

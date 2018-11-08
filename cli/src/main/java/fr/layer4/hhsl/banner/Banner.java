package fr.layer4.hhsl.banner;

import fr.layer4.hhsl.Cluster;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.Data;
import org.springframework.shell.TerminalSizeAware;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Data
public class Banner implements TerminalSizeAware {

    public static final String DEFAULT_BANNER = "<#list 1..width as x>-</#list>\nUsing ${cluster.name}\n<#list 1..width as x>-</#list>";
    private final String template;
    private final Cluster cluster;

    @Override
    public CharSequence render(int terminalWidth) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);

        Map<String, Object> model = new HashMap<>();
        model.put("cluster", this.cluster);
        model.put("width", terminalWidth);

        Template temp;
        try {
            temp = new Template("name", new StringReader(template), configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringWriter out = new StringWriter();
        try {
            temp.process(model, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return out.toString();
    }
}

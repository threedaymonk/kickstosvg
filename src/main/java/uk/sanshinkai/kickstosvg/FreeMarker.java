package uk.sanshinkai.kickstosvg;

import java.util.Locale;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

final class FreeMarker {
    private FreeMarker() {}

    private static Configuration cfg;

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_35);

        // Templates will come from the jar, relative to this class
        cfg.setClassForTemplateLoading(FreeMarker.class, "");

        cfg.setDefaultEncoding("UTF-8");

        cfg.setLocale(Locale.ROOT);

        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Don't log exceptions
        cfg.setLogTemplateExceptions(false);

        cfg.setWrapUncheckedExceptions(true);

        // Do not fall back to higher scopes when reading a null loop variable:
        cfg.setFallbackOnNullLoopVariable(false);
    }

    public static Template getTemplate(String name) throws Exception {
        return cfg.getTemplate(name);
    }
}

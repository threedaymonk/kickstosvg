package uk.sanshinkai.kickstosvg;

import java.util.HashMap;

class TemplateMap extends HashMap<String, Object> {
    TemplateMap() {
        super();
    }

    TemplateMap(String key, Object value) {
        super();
        this.put(key, value);
    }

    public TemplateMap addAttribute(String key, Object value) {
        this.put(key, value);
        return this;
    }
}

package uk.sanshinkai.kickstosvg;

import java.util.ArrayList;
import java.util.List;

class FuriganaString {
    private final List<FuriganaComponent> components;

    FuriganaString(String raw) {
        components = new FuriganaStringParser().parse(raw);
    }

    public String surface() {
        var list = new ArrayList<String>();
        for (var c : components) list.add(c.surface());
        return String.join("", list);
    }

    public List<FuriganaComponent> components() {
        return components;
    }
}

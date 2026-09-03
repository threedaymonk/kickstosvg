package uk.sanshinkai.kickstosvg;

import java.util.Locale;

import org.dom4j.Element;
import org.dom4j.Node;

import uk.sanshinkai.kickstosvg.NumericFormatting;

class Builder {
    private Element obj;

    public Builder(Element el) {
        this.obj = el;
    }

    public Builder(Node n) {
        Element el = (Element) n;
        this.obj = el;
    }

    public Builder element(String name) {
        return wrap(obj.addElement(name));
    }

    public Builder text(String str) {
        return wrap(obj.addText(str));
    }

    public Builder attr(String name, String fmt, Object... args) {
        return wrap(obj.addAttribute(name, String.format(Locale.ROOT, fmt, args)));
    }

    public Builder attr(String name, int value) {
        return attr(name, String.valueOf(value));
    }

    public Builder attr(String name, double value) {
        return attr(name, NumericFormatting.tidy(value));
    }

    public Builder style(String name, String fmt, Object... args) {
        return rawStyle(name, String.format(Locale.ROOT, fmt, args));
    }

    public Builder style(String name, int value) {
        return rawStyle(name, String.valueOf(value));
    }

    public Builder style(String name, double value) {
        return rawStyle(name, NumericFormatting.tidy(value));
    }

    private Builder wrap(Element el) {
        return new Builder(el);
    }

    private Builder rawStyle(String name, String formattedValue) {
        var addition = name + ": " + formattedValue;
        var existing = obj.attribute("style");

        if (existing == null)
            return attr("style", addition);
        else
            return attr("style", existing.getValue() + "; " + addition);
    }
}

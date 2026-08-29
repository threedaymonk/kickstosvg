package uk.sanshinkai.kickstosvg;

import org.dom4j.Element;

public class Builder {
    Element obj;

    public Builder(Element el) {
        this.obj = el;
    }

    public Builder element(String name) {
        return wrap(obj.addElement(name));
    }

    public Builder text(String str) {
        return wrap(obj.addText(str));
    }

    public Builder attr(String name, String fmt, Object... args) {
        return wrap(obj.addAttribute(name, String.format(fmt, args)));
    }

    public Builder attr(String name, int value) {
        return attr(name, String.valueOf(value));
    }

    private Builder wrap(Element el) {
        return new Builder(el);
    }
}

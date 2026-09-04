package uk.sanshinkai.kickstosvg;

import java.util.Locale;

final class NumericFormatting {
    private NumericFormatting() {}

    public static String tidy(int n) {
        return String.format(Locale.ROOT, "%d", n);
    }

    public static String tidy(double d) {
        return tidy(d, 3);
    }

    public static String tidy(double d, int precision) {
        var scale = Math.pow(10, precision);
        var fmt = String.format(Locale.ROOT, "%%.%df", precision);
        var rounded = Math.round(d * scale) / scale;
        return String.format(Locale.ROOT, fmt, rounded)
            .replaceFirst("0+$", "")
            .replaceFirst("\\.$", "");
    }
}

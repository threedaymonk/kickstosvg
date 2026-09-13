package uk.sanshinkai.kickstosvg;

import picocli.CommandLine.ITypeConverter;

class TemplateParamConverter implements ITypeConverter<Object> {
    @Override
    public Object convert(String value) throws Exception {
        if (value.matches("\\d+(\\.\\d+)?"))
            return Double.parseDouble(value);
        else
            return value;
    }
}

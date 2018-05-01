package fr.litarvan.paladin;

import java.io.IOException;

import groovy.lang.GString;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class GStringSerializer extends StdSerializer<GString>
{
    public GStringSerializer()
    {
        this(null);
    }

    public GStringSerializer(Class<GString> t)
    {
        super(t);
    }

    @Override
    public void serialize(GString value, JsonGenerator gen, SerializerProvider provider) throws IOException
    {
        gen.writeString(value.toString());
    }
}

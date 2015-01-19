package org.xmlsh.json;

import java.io.IOException;

import org.xmlsh.util.INamingStrategy;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

public class JsonRenamingParserDelegate extends
        JsonParserDelegate {
    private INamingStrategy mNamingStrategy = INamingStrategy.DefaultNamingStrategy;

    private boolean mTo; 
    public JsonRenamingParserDelegate(JsonParser parser, boolean bTo ) {
        super(parser);
        mTo = bTo ;
    }

    @Override
    public String getCurrentName() throws IOException,
            JsonParseException {
        String name = super.getCurrentName();
        if (name != null) {
            name = mTo ? mNamingStrategy.toXmlName(name) : mNamingStrategy.fromXmlName(name);
        }
        return name;
    }
}
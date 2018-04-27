package fr.litarvan.paladin;

import fr.litarvan.paladin.http.HeaderPair;

public class Header
{
    public final String name;
    public final String value;
    public final HeaderPair[] pairs;

    public Header(String name, String value, HeaderPair[] pairs)
    {
        this.name = name;
        this.value = value;
        this.pairs = pairs;
    }

    @Override
    public String toString()
    {
        return name + ": " + value;
    }
}

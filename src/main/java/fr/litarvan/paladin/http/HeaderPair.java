package fr.litarvan.paladin.http;

public class HeaderPair
{
    private String name;
    private String value;

    public HeaderPair(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }
}

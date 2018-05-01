package fr.litarvan.paladin;

import fr.litarvan.paladin.http.HeaderPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Header
{
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String HOST = "Host";
    public static final String USER_AGENT = "User-Agent";
    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String COOKIE = "Cookie";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_STREAM = "application/octet-stream";
    public static final String CONTENT_TYPE_FORM_DATA = "multipart/form-data";
    public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_HTML = "text/html";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_JAVASCRIPT = "application/javascript";
    public static final String CONTENT_TYPE_TYPESCRIPT = "application/typescript";
    public static final String CONTENT_TYPE_JPEG = "image/jpeg";
    public static final String CONTENT_TYPE_GIF = "image/gif";
    public static final String CONTENT_TYPE_PNG = "image/png";
    public static final String CONTENT_TYPE_SVG = "image/svg";

    private String name;
    private String value;
    private List<HeaderPair> pairs;

    public Header(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public Header(String name, String value, HeaderPair[] pairs)
    {
        this.name = name;
        this.value = value;
        this.pairs = new ArrayList<>(Arrays.asList(pairs));
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public List<HeaderPair> getPairs()
    {
        return pairs;
    }

    public HeaderPair getPair(String name)
    {
        for (HeaderPair pair : pairs)
        {
            if (pair.getName().equalsIgnoreCase(name))
            {
                return pair;
            }
        }

        return null;
    }

    @Override
    public String toString()
    {
        return name + ": " + value;
    }
}

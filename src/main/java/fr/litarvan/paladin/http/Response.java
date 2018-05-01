package fr.litarvan.paladin.http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Response
{
    private int code;
    private List<Header> headers;
    private List<Cookie> cookies;
    private byte[] content;

    public Response()
    {
        this.code = 200;
        this.headers = new ArrayList<>();
        this.cookies = new ArrayList<>();
        this.content = null;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public Header addHeader(String name, String value)
    {
        Header header = new Header(name, value);
        this.addHeader(header);

        return header;
    }

    public void addHeader(Header header)
    {
        this.headers.add(header);
    }

    public void setContentType(String contentType)
    {
        Header result = new Header(Header.CONTENT_TYPE, contentType);
        for (int i = 0; i < headers.size(); i++)
        {
            Header header = headers.get(i);
            if (header.getName().equalsIgnoreCase(Header.CONTENT_TYPE))
            {
                headers.set(i, result);
                return;
            }
        }

        addHeader(result);
    }

    public String getContentType()
    {
        for (Header header : getHeaders())
        {
            if (header.getName().equalsIgnoreCase(Header.CONTENT_TYPE))
            {
                return header.getValue();
            }
        }

        return null;
    }

    public List<Header> getHeaders()
    {
        return headers;
    }

    public byte[] getContent()
    {
        return content;
    }

    public void setContentString(String content)
    {
        this.setContentString(content, Charset.defaultCharset());
    }

    public void setContentString(String content, Charset charset)
    {
        this.setContent(content.getBytes(charset));
    }

    public void setContent(byte[] content)
    {
        this.content = content;
    }

    public Cookie addCookie(String name, String value)
    {
        Cookie cookie = new Cookie(name, value);
        addCookie(cookie);

        return cookie;
    }

    public void addCookie(Cookie cookie)
    {
        this.cookies.add(cookie);
    }

    public List<Cookie> getCookies()
    {
        return cookies;
    }
}

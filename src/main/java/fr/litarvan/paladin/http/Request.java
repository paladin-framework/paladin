package fr.litarvan.paladin.http;

import java.nio.charset.Charset;
import java.util.Map;

import fr.litarvan.paladin.Paladin;

public class Request
{
    private Paladin paladin;

    private String ip;

    private HttpMethod method;
    private String uri;
    private Header[] headers;
    private byte[] content;

    private Map<String, String> params;
    private Cookie[] cookies;

    private Response response;
    private ISession session;

    public Request(Paladin paladin, String ip, HttpMethod method, String uri, Header[] headers, byte[] content, Map<String, String> params, Cookie[] cookies, Response response)
    {
        this.paladin = paladin;
        this.ip = ip;
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.content = content;
        this.params = params;
        this.cookies = cookies;
        this.response = response;
    }

    public Paladin getPaladin()
    {
        return paladin;
    }

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public HttpMethod getMethod()
    {
        return method;
    }

    public void setMethod(HttpMethod method)
    {
        this.method = method;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }

    public Header getHeader(String name)
    {
        for (Header header : headers)
        {
            if (header.getName().equalsIgnoreCase(name))
            {
                return header;
            }
        }

        return null;
    }

    public String getHeaderValue(String name)
    {
        Header header = getHeader(name);

        if (header == null)
        {
            return null;
        }

        return header.getValue();
    }

    public void setHeaders(Header[] headers)
    {
        this.headers = headers;
    }

    public Header[] getHeaders()
    {
        return headers;
    }

    public String getContentString()
    {
        return getContentString(Charset.defaultCharset());
    }

    public String getContentString(Charset charset)
    {
        return new String(content, charset);
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

    public String getParam(String name)
    {
        return params.get(name);
    }

    public Map<String, String> getParams()
    {
        return params;
    }

    public Cookie getCookie(String name)
    {
        for (Cookie cookie : cookies)
        {
            if (cookie.getName().equalsIgnoreCase(name))
            {
                return cookie;
            }
        }

        return null;
    }

    public String getCookieValue(String name)
    {
        Cookie cookie = getCookie(name);

        if (cookie == null)
        {
            return null;
        }

        return cookie.getValue();
    }

    public Cookie[] getCookies()
    {
        return cookies;
    }

    public void setCookies(Cookie[] cookies)
    {
        this.cookies = cookies;
    }

    public ISession getSession()
    {
    	if (session == null) {
    		session = getPaladin().getSessionManager().get(this, response);
    	}

        return session;
    }
}

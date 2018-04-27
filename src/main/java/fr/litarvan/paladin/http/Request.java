package fr.litarvan.paladin.http;

import fr.litarvan.paladin.Header;
import fr.litarvan.paladin.Paladin;
import fr.litarvan.paladin.Session;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Request
{
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_HOST = "Host";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEDAER_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_SET_COOKIE = "Set-Cookie";
    public static final String HEADER_COOKIE = "Cookie";

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

    private Paladin paladin;

    private HttpMethod method;
    private String uri;
    private Header[] headers;
    private byte[] content;

    private Map<String, String> params;
    private Cookie[] cookies;

    public Request(Paladin paladin, HttpMethod method, String uri, Header[] headers, byte[] content)
    {
        this.paladin = paladin;
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.content = content;

        Header cookie = header("Cookie");
        if (cookie != null)
        {
            HeaderPair[] pairs = cookie.pairs;
            cookies = new Cookie[pairs.length];

            for (int i = 0; i < pairs.length; i++)
            {
                HeaderPair pair = pairs[i];
                cookies[i] = new Cookie(pair.name, pair.value);
            }
        }
        else
        {
            cookies = new Cookie[0];
        }

        this.params = new HashMap<>();

        String[] splitURI = uri.split("\\?");

        if (splitURI.length > 1)
        {
            addQueryParams(splitURI[1]);
            this.uri = splitURI[0];
        }

        if (CONTENT_TYPE_FORM_URL_ENCODED.equals(contentType()))
        {
            addQueryParams(stringContent());
        }
    }

    protected void addQueryParams(String query)
    {
        for (String param : query.split("&"))
        {
            String[] split = param.split("=");

            try
            {
                params.put(URLDecoder.decode(split[0], Charset.defaultCharset().name()), URLDecoder.decode(split[1], Charset.defaultCharset().name()));
            }
            catch (UnsupportedEncodingException e)
            {
                // Can't happen
            }
        }
    }

    public Paladin paladin()
    {
        return paladin;
    }

    public HttpMethod method()
    {
        return method;
    }

    public String uri()
    {
        return uri;
    }

    public String param(String name)
    {
        return params.get(name);
    }

    public Set<Map.Entry<String, String>> params()
    {
        return params.entrySet();
    }

    public Header header(String name)
    {
        return Stream.of(headers).filter(h -> h.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Header[] headers(String name)
    {
        return Stream.of(headers).filter(h -> h.name.equalsIgnoreCase(name)).toArray(Header[]::new);
    }

    public String contentType()
    {
        Header header = header("Content-Type");

        if (header == null)
        {
            return null;
        }

        return header.value;
    }

    public Header[] headers()
    {
        return headers;
    }

    public byte[] content()
    {
        return content;
    }

    public String stringContent()
    {
        return stringContent(Charset.defaultCharset());
    }

    public String stringContent(Charset charset)
    {
        return new String(content(), charset);
    }

    public Cookie[] cookies()
    {
        return cookies;
    }

    public Cookie cookie(String name)
    {
        return Stream.of(cookies).filter(c -> c.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public String cookieValue(String name)
    {
        Cookie cookie = cookie(name);
        return cookie == null ? null : cookie.value;
    }

    public Session session(Response response)
    {
        return paladin.getSessionManager().get(this, response);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder(method() + " " + uri() + (content.length > 0 ? "(with " + content.length + " bytes of data)" : ""));

        if (params.size() > 0)
        {
            params.forEach((k, v) -> {
                result.append("\n    ").append(k).append("= ").append(v);
            });
            result.append("\n");
        }

        for (Header header : headers())
        {
            result.append("\n    ").append(header);
        }

        return result.toString();
    }
}

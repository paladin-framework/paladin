package fr.litarvan.paladin.http.server.impl;

import fr.litarvan.paladin.http.Header;
import fr.litarvan.paladin.Paladin;
import fr.litarvan.paladin.http.*;
import org.apache.http.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.protocol.HttpCoreContext.*;

public class HttpRequestHandler implements HttpAsyncRequestHandler<HttpRequest>
{
    private Paladin paladin;

    public HttpRequestHandler(Paladin paladin)
    {
        this.paladin = paladin;
    }

    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request, HttpContext context)
    {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpAsyncExchange httpExchange, HttpContext context) throws IOException
    {
        final HttpResponse httpResponse = httpExchange.getResponse();

        String ip = ((DefaultNHttpServerConnection) context.getAttribute(HTTP_CONNECTION)).getRemoteAddress().getHostAddress();

        Request request = createRequest(ip, httpRequest);
        Response response = createResponse(httpResponse);

        paladin.execute(request, response);

        applyResponse(httpResponse, response);

        httpExchange.submitResponse();
    }

    public Request createRequest(String ip, HttpRequest request) throws IOException
    {
        HttpMethod method = HttpMethod.valueOf(request.getRequestLine().getMethod().toUpperCase());
        String uri = request.getRequestLine().getUri();

        Header[] headers = createHeaders(request.getAllHeaders());

        byte[] data = new byte[0];

        if (request instanceof HttpEntityEnclosingRequest)
        {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream())
            {
                ((HttpEntityEnclosingRequest) request).getEntity().writeTo(out);
                data = out.toByteArray();
            }
        }

        Header cookie = null;
        Cookie[] cookies;

        for (Header header : headers)
        {
            if (header.getName().equalsIgnoreCase(Header.COOKIE))
            {
                cookie = header;
                break;
            }
        }

        if (cookie != null)
        {
            List<HeaderPair> pairs = cookie.getPairs();
            cookies  = new Cookie[pairs.size()];

            for (int i = 0; i < pairs.size(); i++)
            {
                HeaderPair pair = pairs.get(i);
                cookies[i] = new Cookie(pair.getName(), pair.getValue());
            }
        }
        else
        {
            cookies = new Cookie[0];
        }

        Map<String, String> params = new HashMap<>();

        String[] splitURI = uri.split("\\?");

        if (splitURI.length > 1)
        {
            extractParams(params, splitURI[1]);
            uri = splitURI[0];
        }

        Header contentType = null;
        for (Header header : headers)
        {
            if (header.getName().equalsIgnoreCase(Header.CONTENT_TYPE))
            {
                contentType = header;
                break;
            }
        }

        if (contentType != null && Header.CONTENT_TYPE_FORM_URL_ENCODED.equals(contentType.getValue()))
        {
            extractParams(params, new String(data, Charset.defaultCharset()));
        }

        return new Request(paladin, ip, method, uri, headers, data, params, cookies);
    }

    public Response createResponse(HttpResponse httpResponse)
    {
        Response response = new Response();
        response.getHeaders().addAll(Arrays.asList(createHeaders(httpResponse.getAllHeaders())));

        return response;
    }

    public void applyResponse(HttpResponse httpResponse, Response response)
    {
        httpResponse.setStatusCode(response.getCode());

        List<Header> headers = new ArrayList<Header>();
        headers.addAll(response.getHeaders());

        String cookies = "";
        for (int i = 0; i < response.getCookies().size(); i++)
        {
            Cookie cookie = response.getCookies().get(i);
            cookies += cookie.getName() + "=" + cookie.getValue() + (i + 1 < response.getCookies().size() ? "; " : "");
        }

        headers.add(new Header(Header.SET_COOKIE, cookies));

        for (int i = 0; i < headers.size(); i++)
        {
            Header header = headers.get(i);
            String value = "";

            if (header.getValue() != null && !header.getValue().isEmpty())
            {
                value = header.getValue();
            }
            else if (header.getPairs() != null)
            {
                for (int i1 = 0; i1 < header.getPairs().size(); i1++)
                {
                    HeaderPair pair = header.getPairs().get(i1);
                    try
                    {
                        value += pair.getName() + "=" + URLEncoder.encode(pair.getValue(), Charset.defaultCharset().name()) + (i1 + 1 < header.getPairs().size() ? "; " : "");
                    }
                    catch (UnsupportedEncodingException ignored)
                    {
                        // Can't happen
                    }
                }
            }

            httpResponse.addHeader(new BasicHeader(header.getName(), value));
        }

        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(response.getContent()));
        entity.setContentLength(response.getContent().length);

        httpResponse.setEntity(entity);
    }

    protected Header[] createHeaders(org.apache.http.Header[] httpHeaders)
    {
        Header[] headers = new Header[httpHeaders.length];

        for (int i = 0; i < httpHeaders.length; i++)
        {
            org.apache.http.Header header = httpHeaders[i];
            HeaderPair[] pairs = new HeaderPair[header.getElements().length];

            for (int i1 = 0; i1 < header.getElements().length; i1++)
            {
                HeaderElement element = header.getElements()[i1];
                pairs[i1] = new HeaderPair(element.getName(), element.getValue());
            }

            headers[i] = new Header(header.getName(), header.getValue(), pairs);
        }

        return headers;
    }

    protected void extractParams(Map<String, String> params, String query)
    {
        for (String param : query.split("&"))
        {
            String[] split = param.split("=");

            try
            {
                params.put(URLDecoder.decode(split[0], Charset.defaultCharset().name()), split.length > 1 ? URLDecoder.decode(split[1], Charset.defaultCharset().name()) : "");
            }
            catch (UnsupportedEncodingException e)
            {
                // Can't happen
            }
        }
    }
}

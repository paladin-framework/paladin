package fr.litarvan.paladin.http.server.impl;

import fr.litarvan.paladin.Header;
import fr.litarvan.paladin.Paladin;
import fr.litarvan.paladin.http.HeaderPair;
import fr.litarvan.paladin.http.HttpMethod;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;
import fr.litarvan.paladin.http.routing.Route;
import org.apache.http.*;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HttpRequestHandler implements HttpAsyncRequestHandler<HttpRequest>
{
    private Paladin paladin;

    public HttpRequestHandler(Paladin paladin)
    {
        this.paladin = paladin;
    }

    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request, HttpContext context) throws HttpException, IOException
    {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpAsyncExchange httpExchange, HttpContext context) throws HttpException, IOException
    {
        final HttpResponse httpResponse = httpExchange.getResponse();

        Request request = createRequest(httpRequest);
        Response response = new Response();

        Route route = paladin.getRouter().match(request);
        try
        {
            Object result = route.getAction().call(request, response);
            System.out.println(result);

            httpResponse.addHeader("Set-Cookie", "PSession-Token=" + paladin.getSessionManager().getSessions()[0].getToken());
            httpResponse.setEntity(new NStringEntity("Bravo! " + result));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            httpResponse.setEntity(new NStringEntity("Non! " + e.getClass().getName() + ": " + e.getMessage() + "\n    at " + e.getStackTrace()[0]));
        }

        httpExchange.submitResponse();
    }

    public Request createRequest(HttpRequest request) throws IOException
    {
        HttpMethod method = HttpMethod.valueOf(request.getRequestLine().getMethod().toUpperCase());
        Header[] headers = new Header[request.getAllHeaders().length];

        for (int i = 0; i < request.getAllHeaders().length; i++)
        {
            org.apache.http.Header header = request.getAllHeaders()[i];
            HeaderPair[] pairs = new HeaderPair[header.getElements().length];

            for (int i1 = 0; i1 < header.getElements().length; i1++)
            {
                HeaderElement element = header.getElements()[i1];
                pairs[i1] = new HeaderPair(element.getName(), element.getValue());
            }

            headers[i] = new Header(header.getName(), header.getValue(), pairs);
        }

        byte[] data = new byte[0];

        if (request instanceof HttpEntityEnclosingRequest)
        {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream())
            {
                ((HttpEntityEnclosingRequest) request).getEntity().writeTo(out);
                data = out.toByteArray();
            }
        }

        return new Request(paladin, method, request.getRequestLine().getUri(), headers, data);
    }
}

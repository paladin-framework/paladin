package fr.litarvan.paladin;

import com.google.common.collect.Iterators;
import com.google.common.io.BaseEncoding;
import fr.litarvan.paladin.http.Cookie;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.IntFunction;

public class SessionManager implements Iterable<Session>
{
    public static final int TOKEN_LENGTH = 64;

    private Random random;

    private long expirationDelay;
    private List<Session> sessions;

    public SessionManager(long expirationDelay)
    {
        this.random = new SecureRandom();
        this.expirationDelay = expirationDelay;
        this.sessions = new ArrayList<>();
    }

    public SessionManager setExpirationDelay(long expirationDelay)
    {
        this.expirationDelay = expirationDelay;
        return this;
    }

    public long getExpirationDelay()
    {
        return expirationDelay;
    }

    public Session get(Request request, Response response)
    {
        Session session = getByToken(request.cookieValue("PSession-Token"));

        if (session == null)
        {
            session = create();
            // TODO: Add token cookie to response
        }

        return session;
    }

    public Session getByToken(String token)
    {
        return sessions().stream().filter(session -> Objects.equals(token, session.getToken())).findFirst().orElse(null);
    }

    public Session create()
    {
        Session session = new Session(expirationDelay <= 0 ? -1 : System.currentTimeMillis() + expirationDelay, generateToken(TOKEN_LENGTH));
        this.sessions.add(session);

        return session;
    }

    protected String generateToken(int length)
    {
        final byte[] buffer = new byte[length];
        random.nextBytes(buffer);
        return BaseEncoding.base64Url().omitPadding().encode(buffer);
    }

    protected List<Session> sessions()
    {
        sessions.removeIf(Session::isExpired);
        return sessions;
    }

    public Session[] getSessions()
    {
        return sessions.toArray(new Session[0]);
    }

    public <T> T[] getAll(Class<T> type, IntFunction<T[]> generator)
    {
        return sessions().stream().map(session -> session.get(type)).filter(Objects::nonNull).toArray(generator);
    }

    @Override
    public Iterator<Session> iterator()
    {
        return Iterators.forArray(sessions().toArray(new Session[0]));
    }
}

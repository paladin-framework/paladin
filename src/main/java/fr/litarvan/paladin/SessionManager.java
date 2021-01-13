package fr.litarvan.paladin;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import com.google.common.collect.Iterators;
import com.google.common.io.BaseEncoding;

import fr.litarvan.paladin.http.Header;
import fr.litarvan.paladin.http.ISession;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;

public class SessionManager implements ISessionManager, Iterable<Session>
{
    public static final long DEFAULT_SESSION_DURATION = TimeUnit.DAYS.toSeconds(30);
    public static final int TOKEN_LENGTH = 64;

    private Random random;

    private long expirationDelay;

    private List<Session> sessions;

    public SessionManager()
    {
        this.random = new SecureRandom();

        this.expirationDelay = DEFAULT_SESSION_DURATION;
        this.sessions = new ArrayList<>();
    }

    public void setExpirationDelay(long expirationDelay)
    {
        this.expirationDelay = expirationDelay;
    }

    public long getExpirationDelay()
    {
        return expirationDelay;
    }

    public Session get(Request request, Response response)
    {
        String token = request.getHeaderValue(Header.AUTHORIZATION);

        if (token != null)
        {
            if (token.startsWith("Bearer") && token.length() > 7)
            {
                token = token.substring(7);
            }
        }

        Session session = token == null ? null : getByToken(token);

        if (session == null)
        {
            session = createSession(request);
            response.addHeader(Header.PALADIN_TOKEN, session.getToken());
        }

        return session;
    }

    public Session getByToken(String token)
    {
        return sessions().stream().filter(session -> Objects.equals(token, session.getToken())).findFirst().orElse(null);
    }

    public Session createSession(Request request)
    {
        long expiresAt = expirationDelay <= 0 ? -1 : LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000 + expirationDelay;

        Session session = new Session(expiresAt, generateToken(TOKEN_LENGTH));
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

    public void remove(Session session)
    {
        sessions.remove(session);
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

	@Override
	public void destroySession(ISession session)
	{
		this.sessions.remove(session);
	}
}

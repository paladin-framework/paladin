package fr.litarvan.paladin;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Iterators;
import com.google.common.io.BaseEncoding;
import fr.litarvan.paladin.http.Request;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.IntFunction;

public class SessionManager implements Iterable<Session>
{
    public static final int ID_LENGTH = 64;

    private Paladin paladin;
    private Random random;
    private Algorithm algorithm;
    private long expirationDelay;
    private List<Session> sessions;

    public SessionManager(Paladin paladin, Algorithm algorithm, long expirationDelay)
    {
        this.paladin = paladin;
        this.random = new SecureRandom();
        this.algorithm = algorithm;
        this.expirationDelay = expirationDelay;
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

    public Session get(Request request)
    {
        String token = request.getHeaderValue(Header.AUTHORIZATION);

        if (token != null)
        {
            if (token.startsWith("Bearer") && token.length() > 7)
            {
                token = token.substring(7);
            }

            try
            {
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT jwt = verifier.verify(token);

                token = jwt.getToken();
            }
            catch (SignatureVerificationException | TokenExpiredException e)
            {
                return null;
            }
        }

        Session session = getByToken(token);

        if (session == null)
        {
            session = create();
        }

        return session;
    }

    public Session getByToken(String token)
    {
        return sessions().stream().filter(session -> Objects.equals(token, session.getToken())).findFirst().orElse(null);
    }

    public Session create()
    {
        long expiresAt = expirationDelay <= 0 ? -1 : LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + expirationDelay;

        Session session = new Session(expiresAt, generateToken(expiresAt));
        this.sessions.add(session);

        return session;
    }

    protected String generateKey()
    {
        final byte[] buffer = new byte[ID_LENGTH];
        random.nextBytes(buffer);

        return BaseEncoding.base64Url().omitPadding().encode(buffer);
    }

    protected String generateToken(long expiresAt)
    {
        return JWT.create()
                  .withClaim("jti", generateKey())
                  .withIssuer(paladin.getApp().getName() + " (Paladin)")
                  .withIssuedAt(new Date(System.currentTimeMillis()))
                  .withExpiresAt(expiresAt > 0 ? new Date(expiresAt) : null)
                  .sign(algorithm);
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
}

package fr.litarvan.paladin;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Iterators;
import com.google.common.io.BaseEncoding;
import fr.litarvan.paladin.http.Header;
import fr.litarvan.paladin.http.Request;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

public class SessionManager implements Iterable<Session>
{
    public static final long DEFAULT_SESSION_DURATION = TimeUnit.DAYS.toSeconds(30);
    public static final int ID_LENGTH = 64;

    private Random random;

    private Paladin paladin;
    private Algorithm algorithm;
    private long expirationDelay;

    private List<Session> sessions;

    public SessionManager(Paladin paladin, String secret)
    {
        this.random = new SecureRandom();

        this.paladin = paladin;
        this.expirationDelay = DEFAULT_SESSION_DURATION;
        this.sessions = new ArrayList<>();

        try
        {
            this.algorithm = Algorithm.HMAC256(secret);
        }
        catch (UnsupportedEncodingException e)
        {
            // Can happen very rarely, but it can

            e.printStackTrace();
            System.exit(0);
        }
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
                token = null;
            }
        }

        Session session = token == null ? null : getByToken(token);

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
        long expiresAt = expirationDelay <= 0 ? -1 : LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000 + expirationDelay;

        Session session = new Session(expiresAt, generateToken(expiresAt));
        this.sessions.add(session);

        return session;
    }

    protected String generateKey(int length)
    {
        final byte[] buffer = new byte[length];
        random.nextBytes(buffer);

        return BaseEncoding.base64Url().omitPadding().encode(buffer);
    }

    protected String generateToken(long expiresAt)
    {
        return JWT.create()
                  .withClaim("jti", generateKey(ID_LENGTH))
                  .withIssuer(paladin.getAppInfo().name() + " (Paladin)")
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

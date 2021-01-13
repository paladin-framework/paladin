package fr.litarvan.paladin;

import fr.litarvan.paladin.http.ISession;
import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;

public interface ISessionManager {

	ISession get(Request request, Response response);
	ISession createSession(Request request);
	void destroySession(ISession session);
	void setExpirationDelay(long expirationDelay);
}

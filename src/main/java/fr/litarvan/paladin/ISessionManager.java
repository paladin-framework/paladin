package fr.litarvan.paladin;

import fr.litarvan.paladin.http.Request;
import fr.litarvan.paladin.http.Response;

public interface ISessionManager {

	Session get(Request request, Response response);

	long getExpirationDelay();

	void setExpirationDelay(long expirationDelay);
}

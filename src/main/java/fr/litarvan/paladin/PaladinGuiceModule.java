package fr.litarvan.paladin;

import com.google.inject.AbstractModule;
import fr.litarvan.paladin.http.routing.Router;

public class PaladinGuiceModule extends AbstractModule
{
    private Paladin paladin;

    public PaladinGuiceModule(Paladin paladin)
    {
        this.paladin = paladin;
    }

    @Override
    protected void configure()
    {
        bind(Paladin.class).toInstance(paladin);
        bind(ConfigManager.class).toInstance(paladin.getConfigManager());
        bind(Router.class).toInstance(paladin.getRouter());
        bind(SessionManager.class).toInstance(paladin.getSessionManager());
    }
}

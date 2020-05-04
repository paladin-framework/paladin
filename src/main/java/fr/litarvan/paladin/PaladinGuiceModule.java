package fr.litarvan.paladin;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

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
        bind(PaladinConfig.class).toInstance(paladin.getConfig());
        bind(Router.class).toInstance(paladin.getRouter());
        bind(ISessionManager.class).toInstance(paladin.getSessionManager());

        bind(Object.class).annotatedWith(InjectApp.class).toProvider(new Provider<Object>()
        {
            @Override
            public Object get()
            {
                return paladin.getApp();
            }
        });
    }
}

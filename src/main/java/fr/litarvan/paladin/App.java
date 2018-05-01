package fr.litarvan.paladin;

import javax.inject.Inject;

public abstract class App
{
    @Inject
    protected Paladin paladin;

    public abstract void onStart();
    public abstract void onStop();

    public abstract String getName();
    public abstract String getVersion();
}

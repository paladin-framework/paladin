package fr.litarvan.paladin;

public class PaladinConfig
{
    public final int port;
    public final boolean disableLogs;

    public PaladinConfig(int port, boolean disableLogs)
    {
        this.port = port;
        this.disableLogs = disableLogs;
    }
}

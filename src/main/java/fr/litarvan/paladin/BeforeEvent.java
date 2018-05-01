package fr.litarvan.paladin;

public class BeforeEvent
{
    private boolean cancelled = false;
    private Object result;

    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    public void setResult(Object result)
    {
        this.result = result;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public Object getResult()
    {
        return result;
    }
}

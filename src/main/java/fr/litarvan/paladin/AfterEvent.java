package fr.litarvan.paladin;

public class AfterEvent
{
    private Object result;

    public AfterEvent(Object result)
    {
        this.result = result;
    }

    public Object getResult()
    {
        return result;
    }

    public void setResult(Object result)
    {
        this.result = result;
    }
}

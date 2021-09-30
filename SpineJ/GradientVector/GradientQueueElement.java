package SpineJ.GradientVector;

final class GradientQueueElement
{

    GradientQueueElement()
    {
        iCapacity = 40;
        iLast = -1;
        iarray = new int[iCapacity];
    }

    int add(int i)
    {
        if(++iLast == iCapacity)
            inccap();
        iarray[iLast] = i;
        return iLast;
    }

    private void inccap()
    {
        iCapacity += 40;
        int ai[] = new int[iCapacity];
        for(int i = 0; i < iLast; i++)
            ai[i] = iarray[i];

        iarray = ai;
    }

    int get(int i)
    {
        return iarray[i];
    }

    int remove()
    {
        return iarray[iLast--];
    }

    int remove(int i)
    {
        int j = iarray[i];
        iarray[i] = iarray[iLast--];
        return j;
    }

    int size()
    {
        return iLast + 1;
    }

    private int iCapacity;
    private final int iCapInc = 40;
    private int iLast;
    private int iarray[];
}
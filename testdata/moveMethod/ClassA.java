class ClassA
{
    static void methodA1()
    {
        attributeA1=0;
        methodA2();
    }

    static void methodA2()
    {
        attributeA2=0;
        attributeA1=0;
    }

    static void methodA3()
    {
        attributeA1=0;
        attributeA2=0;
        methodA1();
        methodA2();
    }

    static int attributeA1;
    static int attributeA2;
}

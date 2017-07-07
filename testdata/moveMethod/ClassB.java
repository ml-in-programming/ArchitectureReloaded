class ClassB
{
    static void methodB1()
    {
        ClassA.attributeA1=0;
        ClassA.attributeA2=0;
        ClassA.methodA1();
    }

    static void methodB2()
    {
        attributeB1=0;
        attributeB2=0;
    }

    static void methodB3()
    {
        attributeB1=0;
        methodB1();
        methodB2();
    }

    static int attributeB1;
    static int attributeB2;
}

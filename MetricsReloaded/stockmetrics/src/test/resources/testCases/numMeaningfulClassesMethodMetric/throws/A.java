class A {
    void method1() {
    }

    void method2() throws MyException {
    }

    void method3() throws MyException, MyException {
    }

    void method4() throws MyThrowable, MyException {
    }

    void method5() throws Exception {
    }

    private class MyException extends Exception {

    }

    private class MyThrowable extends Throwable {

    }
}

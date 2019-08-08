package java.lang;

public class Strings {
    public void hello() {
        System.out.println("i am loaded by " + getClass().getClassLoader().getClass());
    }
}

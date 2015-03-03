package ru.ifmo.ctddev.peresadin;

import java.io.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.NavigableSet;

/**
 * Created by pva701 on 3/1/15.
 */
public class Implementor {

    abstract class C {
        protected abstract int f();
    }

    interface B {
        public void fun();
    }

    interface Int {
        void f();
    }

    static abstract class Ab {
        final int y;
        public Ab(int x) {y = x;}
        public abstract int f() throws IOException;
    }

    static interface Bb {
        abstract java.util.List<String> list(java.util.List<String> x);
        public abstract int f() throws CloneNotSupportedException;
    }

    abstract static class EbaniyTmp extends Ab implements Bb {
        public EbaniyTmp(int x) {
            super(x);
        }

        @Override
        public int f() {
            return 0;
        }
    }

    /*abstract class Ce extends Bb {
        public Ce(int e) {super(e);}
        public int f() {return 2;}

    }
    abstract class Cc extends Bb {
        Cc(int e) {
            super(e);
        }
        int e() {
            return 20;
        }
    }*/


    interface X1 {
        int x1();
    }

    interface X1Impl extends X1 {
        int x2();
    }

    public abstract static class AbX1 {
        public abstract int x2();
        public AbX1(int x) {}
    }

    public static abstract class X1C implements X1, X1Impl {
    }

    interface X1ImplImpl extends X1Impl {
        public int x2();
    }

    class XXX implements X1 {
        public int x1() {
            return 2;
        }
    }

    static abstract class X2 implements X1 {
        abstract public int x2();
        public int x1() {return 2;}
    }

    class X3 extends X2 implements X1 {
        public int x2() {
            return 2;
        }

    }

    static interface NavSetInt extends NavigableSet<Integer> {

    }

    public static void main(String[] args) {
        try {
            Class c = AbX1.class;
            Writer wr = new PrintWriter(new File(c.getSimpleName() + "Impl" + ".java"));
            new ImplBuilder(c).implement(wr);
            wr.close();
        } catch (IOException e) {
            System.out.println("exception = " + e.toString());
        }

        /*Class c = Bb.class;
        System.out.println("ass = " + X1.class.isAssignableFrom(c));
        System.out.println("is eq = " + c.getDeclaredMethods()[0].equals(X1.class.getDeclaredMethods()[0]));
        for (Method m : c.getDeclaredMethods()) {
            System.out.println("nam = " + m.getName());
            //System.out.println(m.getGenericReturnType());
            //System.out.println("len = " + m.getTypeParameters().length);
            //if (m.getGenericParameterTypes().length != 0)
                //System.out.println(m.getGenericParameterTypes()[0].toString());
        }

        System.out.println("==============");
        for (Method m : c.getMethods())
            System.out.println(m.toString());*/

    }
}

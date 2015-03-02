package ru.ifmo.ctddev.peresadin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.*;

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

    static class A {
        public static int f(int x, double y) {
            B obj = new B() {
                @Override
                public void fun() {

                }
            };
            System.out.println(obj.getClass().getEnclosingMethod().toGenericString());
            return 0;
        }
    }

    interface Int {
        void f();
    }

    static abstract class Ab {
        final int y;
        public Ab(int x) {y = x;}
        protected abstract int f();
    }

    static class Cp extends Ab {
        Cp(int x) {super(x);}
        public int f() {return 2;}
    }

    static abstract class Bb extends Ab {
        Bb(int x) {
            super(x);
        }
        abstract <T> java.util.List<T> list(java.util.List<T> x);
        public abstract int f();
        //private void g() {}
        //protected void k() {}

        //abstract List<String> list(List<String> x);
    }

   /* class Ce extends Bb {
        public Ce(int e) {super(e);}
        public int f() {return 2;}

    }*/
    /*class Cc extends Bb {
        Cc(int e) {
            super(e);
        }
        int e() {
            return 20;
        }
    }*/


    interface X1 {
        public int x1();
    }

    interface X1Impl extends X1 {
        public int x1();
    }

    class X1C implements X1 {
        public int x1() {
            return 1;
        }
    }

    class TT extends X1C implements X1Impl {

    }

    interface X1ImplImpl extends X1Impl {
        public int x2();
    }

    /*class XXX implements X1 {
        public int x1() {
            return 2;
        }
    }*/

    abstract class X2 implements X1 {
        abstract int x2();
        public int x1() {return 2;}
    }

    /*class X3 extends X2 implements X1 {
        int x2() {
            return 2;
        }

    }*/

    public static void main(String[] args) {

        Class c = Bb.class;
        System.out.println("ass = " + X1.class.isAssignableFrom(c));
        System.out.println("is eq = " + c.getDeclaredMethods()[0].equals(X1.class.getDeclaredMethods()[0]));
        for (Method m : c.getDeclaredMethods()) {
            /*Type[] smth = m.getGenericParameterTypes();
            System.out.println("tp");
            for (int i = 0; i < smth.length; ++i)
                System.out.println(smth[i].getName());*/
            System.out.println(m.getGenericReturnType());
            System.out.println("len = " + m.getTypeParameters().length);
            //if (m.getGenericParameterTypes().length != 0)
                //System.out.println(m.getGenericParameterTypes()[0].toString());
        }

        System.out.println("==============");
        for (Method m : c.getMethods())
            System.out.println(m.toString());
    }
}

package ru.ifmo.ctddev.peresadin;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Created by pva701 on 3/1/15.
 */
public class Implementor {
    private Class baseClass;
    public static String SPACE = "    ";

    public Implementor(Class cl) {
        this.baseClass = cl;
    }

    public void implement(Writer writer) throws IOException {
        writer.append("package impl;\n");

        String head = "class " + baseClass.getSimpleName() + "Impl ";
        if (baseClass.isInterface()) head += "implements " + baseClass.getCanonicalName() + " { ";
        else head += "extends " + baseClass.getCanonicalName() + " { ";
        writer.write(head + "\n");

        implementConstructor(writer);

        ArrayList<Method> methods = getNotImplementedMethods();
        for (Method m : methods) {
            String nameMethod = printDeclarationOfMethod(m);
            writer.append(SPACE).append(nameMethod).append(" {\n");
            String retValue;
            if (!m.getReturnType().isPrimitive()) {
                retValue = "null";
            } else {
                retValue = getDefaultValue(m.getReturnType());
            }
            writer.append(SPACE).append(SPACE).append("return ").append(retValue).append(";\n");
            writer.append(SPACE).append("}\n\n");
        }
        writer.append("}");

    }

    private void implementConstructor(Writer writer) throws IOException{
        if (baseClass.getDeclaredConstructors().length == 0)
            return;
        Constructor constructor = baseClass.getDeclaredConstructors()[0];
        writer.append(SPACE).append(printDeclarationOfConstructor(constructor)).append(" {\n");

        final int ALPHABET = 26;
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int args = constructor.getGenericParameterTypes().length;
        for (int j = 0; j < args; j++) {
            String varName = "" + (char)('a' + j % ALPHABET);
            if (j / ALPHABET > 0) varName += j / ALPHABET;
            sb.append(varName);
            if (j + 1 != args)
                sb.append(',');
        }
        sb.append(");");
        String superStr = "super" + sb.toString();
        writer.append(SPACE).append(SPACE).append(superStr + "\n");
        writer.append(SPACE).append("}\n\n");
    }

    public String printDeclarationOfConstructor(Constructor constructor) {
        StringBuilder sb = new StringBuilder();
        printModifiers(sb, constructor);
        sb.append(baseClass.getSimpleName() + "Impl");
        printParameters(sb, constructor);
        return sb.toString();
    }

    private String printDeclarationOfMethod(Method m) {
        StringBuilder sb = new StringBuilder();
        printModifiers(sb, m);
        printHeader(sb, m);
        printParameters(sb, m);
        //printExceptions(sb, m);
        return sb.toString();
    }

    private void printModifiers(StringBuilder sb, Executable m) {
        if (Modifier.isPublic(m.getModifiers()))
            sb.append("public ");
        else if (Modifier.isProtected(m.getModifiers()))
            sb.append("protected ");
        if (Modifier.isPrivate(m.getModifiers()))
            sb.append("private ");
    }

    private void printHeader(StringBuilder sb, Method m) {
        Type genRetType = m.getGenericReturnType();
        sb.append(genRetType.getTypeName()).append(' ');
        //sb.append(m.getDeclaringClass().getTypeName()).append('.');
        sb.append(m.getName());
    }

    private void printParameters(StringBuilder sb, Executable m) {
        final int ALPHABET = 26;
        sb.append('(');
        Type[] params = m.getGenericParameterTypes();
        for (int j = 0; j < params.length; j++) {
            String varName = "" + (char)('a' + j % ALPHABET);
            if (j / ALPHABET > 0) varName += j / ALPHABET;
            String param = params[j].getTypeName();
            sb.append(param).append(" ").append(varName);
            if (j < params.length - 1)
                sb.append(',');
        }
        sb.append(')');
    }

    private void printExceptions(StringBuilder sb, Method m) {
        Type[] exceptions = m.getGenericExceptionTypes();
        if (exceptions.length > 0) {
            sb.append(" throws ");
            for (int k = 0; k < exceptions.length; k++) {
                sb.append(((Class) exceptions[k]).getName());
                if (k + 1 != exceptions.length)
                    sb.append(',');
            }
        }
    }

    public static boolean bIsImplementationOfA(Method a, Method b) {
        int aMod = a.getModifiers();
        int bMod = b.getModifiers();
        if (Modifier.isAbstract(aMod) &&
                a.getName().equals(b.getName()) &&
                a.getReturnType().equals(b.getReturnType())) {

            Class[] parameterTypesA = a.getParameterTypes();
            Class[] parameterTypesB = b.getParameterTypes();
            if (parameterTypesA.length != parameterTypesB.length)
                return false;
            for (int i = 0; i < parameterTypesA.length; ++i)
                if (parameterTypesA[i] != parameterTypesB[i])
                    return false;
            return a.getDeclaringClass().isAssignableFrom(b.getDeclaringClass());
        }
        return false;
    }

    private void getNotImplementedMethods(Class c, ArrayList<Method> methods) {
        if (c == null)
            return;
        getNotImplementedMethods(c.getSuperclass(), methods);
        Class[] interfaces = c.getInterfaces();
        for (Class interf : interfaces)
            for (Method m : interf.getMethods())
                safeAdd(methods, m);

        for (Method m : c.getDeclaredMethods())
            safeAdd(methods, m);
    }

    public ArrayList<Method> getNotImplementedMethods() {
        ArrayList<Method> methods = new ArrayList<Method>();
        getNotImplementedMethods(baseClass, methods);
        ArrayList<Method> publics = new ArrayList<Method>();
        //for (Method m : methods) System.out.println("" + m.toString());

        for (Method m : methods)
            if (Modifier.isPublic(m.getModifiers())) {
                boolean dontAdd = false;
                for (Method pm : publics)
                    if (methodEquals(pm, m)) {
                        dontAdd = true;
                        break;
                    }
                if (!dontAdd) publics.add(m);
            }

        for (Method m : methods)
            if (Modifier.isProtected(m.getModifiers())) {
                boolean dontAdd = false;
                for (Method pm : publics)
                    if (methodEquals(pm, m)) {
                        dontAdd = true;
                        break;
                    }
                if (!dontAdd) publics.add(m);
            }
        /*System.out.println("=======================");
        for (Method m : publics)
            System.out.println("" + m.toString());*/
        return publics;
    }

    private boolean methodEquals(Method m1, Method m2) {
        StringBuilder sm1 = new StringBuilder();
        StringBuilder sm2 = new StringBuilder();
        printHeader(sm1, m1);
        printParameters(sm1, m1);
        printHeader(sm2, m2);
        printParameters(sm2, m2);
        return sm1.toString().equals(sm2.toString());
    }

    private void safeAdd(ArrayList<Method> methods, Method method) {
        int mod = method.getModifiers();
        if (Modifier.isAbstract(mod)) {
            methods.add(method);
            return;
        }
        for (Iterator<Method> it = methods.iterator(); it.hasNext(); ) {
            Method cur = it.next();
            if (bIsImplementationOfA(cur, method))
                it.remove();
        }
    }

    private static final Map<Class<?>, String> DEFAULT_VALUES;

    static {
        Map<Class<?>, String> defaults = new HashMap<Class<?>, String>();
        defaults.put(boolean.class, "false");
        defaults.put(char.class, "0");
        defaults.put(byte.class, "false");
        defaults.put(short.class, "0");
        defaults.put(int.class, "0");
        defaults.put(long.class, "0L");
        defaults.put(float.class, "0f");
        defaults.put(double.class, "0d");
        DEFAULT_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static String getDefaultValue(Class<?> type) {
        return DEFAULT_VALUES.get(type);
    }

    public static void main(String[] args) throws ClassNotFoundException {
        //Class c = Class.forName(args[0]);
        //Class c = NavigableSet.class;

        Class c = NavigableSet.class;
        try {
            Writer wr = new PrintWriter(new File(c.getSimpleName() + "Impl" + ".java"));
            new Implementor(c).implement(wr);
            wr.close();
        } catch (IOException e) {
        }
    }

    public static abstract class ClDef {
        public abstract int f();
    }
}

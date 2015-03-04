package ru.ifmo.ctddev.peresadin;
import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by pva701 on 3/1/15.
 */
public class Implementor implements Impler {
    private Class baseClass;
    public static String SPACE = "    ";

    public Implementor() {}

    public Implementor(Class cl) {
        this.baseClass = cl;
    }

    @Override
    public void implement(Class<?> token, File root) throws ImplerException {
        if (token.getPackage() == null)
            throw new ImplerException();
        baseClass = token;
        String packagePath = "/" + token.getPackage().getName().replace('.', '/');
        String classPath = root.getAbsoluteFile() + packagePath + "/" + token.getSimpleName() + "Impl.java";
        File f = new File(classPath);
        f.getParentFile().mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("File doesn't create!");
        }

        try (PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new FileWriter(classPath)
                )))
        {
            implement(writer);
        } catch (IOException e) {
            System.out.println("ex = " + e.getMessage());
        }
    }

    public void implement(Writer writer) throws IOException, ImplerException {
        if (Modifier.isFinal(baseClass.getModifiers()))
            throw new ImplerException();
        writer.append("package " + baseClass.getPackage().getName() + ";\n");
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

    private void implementConstructor(Writer writer) throws IOException, ImplerException {
        if (baseClass.getDeclaredConstructors().length == 0)
            return;
        Constructor[] constructors = baseClass.getDeclaredConstructors();
        Constructor constructor = null;
        for (int i = 0; i < constructors.length; ++i)
            if (!Modifier.isPrivate(constructors[i].getModifiers())) {
                constructor = constructors[i];
                break;
            }
        if (constructor == null)
            throw new ImplerException();
        writer.append(SPACE).append(printDeclarationOfConstructor(constructor)).append(" {\n");

        final int ALPHABET = 26;
        StringBuilder sb = new StringBuilder();
        int args = constructor.getGenericParameterTypes().length;
        String[] types = new String[args];
        for (int i = 0; i < args; ++i) types[i] = "";
        printParameters(sb, types);
        sb.append(";");
        String superStr = "super" + sb.toString();

        writer.append(SPACE).append(SPACE).append(superStr + "\n");
        writer.append(SPACE).append("}\n\n");
    }

    private void printParameters(StringBuilder sb, String[] types) {
        final int ALPHABET = 26;
        sb.append('(');
        for (int j = 0; j < types.length; j++) {
            String varName = "" + (char)('a' + j % ALPHABET);
            if (j / ALPHABET > 0) varName += (char)('0' + j / ALPHABET + 1);
            sb.append(types[j]).append(" ").append(varName);
            if (j < types.length - 1)
                sb.append(',');
        }
        sb.append(')');
    }

    public String printDeclarationOfConstructor(Constructor constructor) {
        StringBuilder sb = new StringBuilder();
        printModifiers(sb, constructor);
        sb.append(baseClass.getSimpleName() + "Impl");
        printParameters(sb, constructor);
        printExceptions(sb, constructor);
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
        int types = m.getGenericParameterTypes().length;
        Type[] params = m.getGenericParameterTypes();
        String[] typesArr = new String[types];
        for (int i = 0; i < types; ++i)
            typesArr[i] = params[i].getTypeName();
        printParameters(sb, typesArr);
    }

    private void printExceptions(StringBuilder sb, Executable m) {
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
        defaults.put(void.class, "");
        DEFAULT_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static String getDefaultValue(Class<?> type) {
        return DEFAULT_VALUES.get(type);
    }


    public static void main(String[] args) throws ClassNotFoundException,ImplerException {
        //Class c = Class.forName(args[0]);
        //Class c = NavigableSet.class;
        try {
            Class c = Cl.class;
            Writer wr = new PrintWriter(new File(c.getSimpleName() + "Impl" + ".java"));
            new Implementor(c).implement(wr);
            wr.close();
        } catch (IOException e) {
            System.out.println("exception");
        }
    }

    public static abstract class Cl implements NavigableSet<Integer> {
    }
}

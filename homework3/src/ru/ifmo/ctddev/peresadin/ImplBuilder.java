package ru.ifmo.ctddev.peresadin;

import sun.invoke.util.Wrapper;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by pva701 on 3/1/15.
 */
public class ImplBuilder {
    private Class baseClass;
    private String currentIndent = "";
    public static String SPACE = "    ";

    public ImplBuilder(Class cl) {
        this.baseClass = cl;
    }

    public String getName() {
        return baseClass.getCanonicalName();
    }

    private void implement(Writer writer) throws IOException {
        String head = "class " + getName() + "Impl ";
        if (baseClass.isInterface()) head += "implements " + getName() + " { ";
        else head += "extends " + getName() + " { ";
        writer.write(head + "\n");
        ArrayList<Method> methods = getNotImplementedMethods();
        for (Method m : methods) {
            String nameMethod = genDeclarationMethod(m);
            writer.append(currentIndent).append(nameMethod).append(" {\n");
            String retValue = "";
            if (!m.getReturnType().isPrimitive())
                retValue = "null";
            else {
                try {
                 retValue = m.getReturnType().newInstance() + "";
                } catch (Exception ignore) {}
            }
            writer.append(currentIndent).append(SPACE).append("return ").append(retValue).append(";\n}\n\n");
        }

    }

    public String genDeclarationMethod(Method m) {
        StringBuilder sb = new StringBuilder();
        printModifiers(sb, m);
        printHeader(sb, m);
        //TODO print types
        printExceptions(sb, m);
        return sb.toString();
    }

    private void printModifiers(StringBuilder sb, Method m) {
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
        sb.append(m.getDeclaringClass().getTypeName()).append('.');
        sb.append(getName());
    }

    private void printExceptions(StringBuilder sb, Method m) {
        Type[] exceptions = m.getGenericExceptionTypes();
        if (exceptions.length > 0) {
            sb.append(" throws ");
            for (int k = 0; k < exceptions.length; k++) {
                sb.append(exceptions[k].toString());
                if (k + 1 != exceptions.length - 1)
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
        Class[] interfaces = c.getInterfaces();
        for (Class interf : interfaces)
            for (Method m : interf.getMethods())
                safeAdd(methods, m);
        getNotImplementedMethods(c.getSuperclass(), methods);
        for (Method m : c.getDeclaredMethods())
            safeAdd(methods, m);
    }

    public ArrayList<Method> getNotImplementedMethods() {
        ArrayList<Method> methods = new ArrayList<Method>();
        getNotImplementedMethods(baseClass, methods);
        return methods;
    }

    private void safeAdd(ArrayList<Method> methods, Method method) {
        int mod = method.getModifiers();
        boolean found = false;
        for (Iterator<Method> it = methods.iterator(); it.hasNext(); ) {
            Method cur = it.next();
            if (bIsImplementationOfA(cur, method) && !Modifier.isAbstract(mod)) {
                it.remove();
                found = true;
                break;
            } else if (bIsImplementationOfA(method, cur)) {
                it.remove();
                break;
            }
        }
        if (!found) methods.add(method);
    }
}

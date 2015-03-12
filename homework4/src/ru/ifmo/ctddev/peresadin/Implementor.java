package ru.ifmo.ctddev.peresadin;
import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;
import org.junit.Assert;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.Manifest;

/**
 * {@code Implementor} generate default implementation of abstract classes and interfaces.
 *
 * <p>{@code Implementor} implements all unimplemented abstract methods of class.
 * No support of generic classes, but supported generic methods.</p>
 *
 * @author Ilya Peresadin
 */

public class Implementor implements JarImpler {
    /**
     * Implemented class
     */
    private Class baseClass;
    /**
     * Tab for generating code.
     */
    public static String SPACE = "    ";

    /**
     * Constructs a new instance.
     * @param implClass the class for which need to implement.
     */
    public Implementor(Class implClass) {
        this.baseClass = implClass;
    }

    /**
     * @param token type token to create implementation for
     * @param root root directory
     * @throws ImplerException if it wasn't possible implement of class
     */
    @Override
    public void implement(Class<?> token, File root) throws ImplerException {
        if (token.getPackage() == null)
            throw new ImplerException();
        baseClass = token;
        String packagePath = File.separatorChar + token.getPackage().getName().replace('.', File.separatorChar);
        String classPath = root.getAbsoluteFile() + packagePath + File.separatorChar + token.getSimpleName() + "Impl.java";
        File f = new File(classPath);
        f.getParentFile().mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("File doesn't create!");
        }

        try (BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(classPath), StandardCharsets.UTF_8)))
        {
            implement(writer);
        } catch (IOException e) {
            System.out.println("ex = " + e.getMessage());
        }
    }

    @Override
    public void implementJar(Class<?> token, File jarFile) throws ImplerException {

        try (
            Writer writer = new BufferedWriter(new FileWriter(token.getSimpleName() + "Impl.java"))) {
            implement(writer);
        } catch (IOException e) {
            System.out.println(" io excpetion ");
        }

        String classFileName = token.getSimpleName() + "Impl.class";
        String packageName = token.getPackage().getName();
        String root = packageName.substring(0, packageName.indexOf('.'));
        String fullDir = token.getPackage().getName().replace('.', File.separatorChar);
        File f = new File(fullDir);
        f.mkdirs();
        compile(token.getSimpleName() + "Impl.java");
        File fullPathClassFile = new File(f.getAbsolutePath() + File.separatorChar + classFileName);
        try {
            Files.move(new File(classFileName).toPath(), fullPathClassFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("move exception!");
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (
                JarOutputStream jar = new JarOutputStream(new FileOutputStream(jarFile), manifest);
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(fullPathClassFile));
        ) {
            JarEntry entry = new JarEntry(fullDir + File.separatorChar + classFileName);
            jar.putNextEntry(entry);
            byte[] buffer = new byte[1024];
            while (true)
            {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                jar.write(buffer, 0, count);
            }
            jar.closeEntry();
            jar.flush();
        } catch (IOException e){
            System.out.println("exception jar");
            e.printStackTrace();
        } finally {
            clean(new File(root));
        }
    }

    /**
     * Implements class and write implementation in {@code writer}.
     * @param  writer - {@link java.io.Writer} which consumes generated implementation code
     * @throws java.io.IOException if some problems with {@code writer}
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException if implemented class
     * is final, primitive or contains only private constructors.
     */
    public void implement(Writer writer) throws IOException, ImplerException {
        if (Modifier.isFinal(baseClass.getModifiers())) {
            throw new ImplerException();
        }
        writer.append("package " + baseClass.getPackage().getName() + ";\n");
        writer.write(
                "class " + baseClass.getSimpleName() + "Impl " + (baseClass.isInterface() ? "implements" : "extends") +
                        " " + baseClass.getCanonicalName() + " {\n"
        );

        implementConstructor(writer);

        List<Method> methods = getNotImplementedMethods();
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

    /**
     * Writes in {@code writer} implementation of non-private constructor of {@code baseClass}
     * or throws ImplerException if implemented class contains only private constructors.
     * @param writer {@link java.io.Writer} which consumes generated implementation code
     * @throws IOException if some problems with {@code writer}
     * @throws ImplerException if has only private constructors
     */
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

    /**
     * Appends in {@link java.lang.StringBuilder} arguments and their types of method.
     * Arguments will be called a, b,...,z, a1, b1 etc.
     * @param sb the {@code StringBuilder} in which will be stored result
     * @param types types of arguments
     */
    private void printParameters(StringBuilder sb, String[] types) {
        final int ALPHABET = 26;
        sb.append('(');
        for (int j = 0; j < types.length; j++) {
            String varName = "" + (char)('a' + j % ALPHABET);
            if (j / ALPHABET > 0) {
                varName += (char)('0' + j / ALPHABET + 1);
            }
            sb.append(types[j]).append(" ").append(varName);
            if (j < types.length - 1) {
                sb.append(',');
            }
        }
        sb.append(')');
    }

    /**
     * Implements declaration of constructor.
     * @param constructor the constructor for which it is necessary to generate declaration
     * @return the declaration of {@code constructor}
     */
    public String printDeclarationOfConstructor(Constructor constructor) {
        StringBuilder sb = new StringBuilder();
        printModifiers(sb, constructor);
        sb.append(baseClass.getSimpleName()).append("Impl");
        printParameters(sb, constructor);
        printExceptions(sb, constructor);
        return sb.toString();
    }

    /**
     * Implements declaration of method.
     * @param method the method for which it is necessary to generate declaration
     * @return the declaration of {@code method}
     */
    private String printDeclarationOfMethod(Method method) {
        StringBuilder sb = new StringBuilder();
        printModifiers(sb, method);
        printGenericTypes(sb, method);
        printHeader(sb, method);
        printParameters(sb, method);
        //printExceptions(sb, m);
        return sb.toString();
    }

    /**
     * Generates generic types for method.
     * @param sb the {@code StringBuilder} in which will be stored result
     * @param method the method for which it is necessary to generate generic types
     */
    private void printGenericTypes(StringBuilder sb, Method method) {
        TypeVariable<?>[] typeparms = method.getTypeParameters();
        if (typeparms.length > 0) {
            boolean first = true;
            sb.append("<");
            for(TypeVariable<?> typeparm: typeparms) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(typeparm.toString());
                first = false;
            }
            sb.append("> ");
        }
    }

    /**
     * Prints modifiers of executable object in {@code StringBuilder}.
     * @param sb the {@code StringBuilder} in which will be stored result
     * @param exec method or constructor for which it is necessary print modifiers
     */
    private void printModifiers(StringBuilder sb, Executable exec) {
        if (Modifier.isPublic(exec.getModifiers())) {
            sb.append("public ");
        } else if (Modifier.isProtected(exec.getModifiers())) {
            sb.append("protected ");
        } if (Modifier.isPrivate(exec.getModifiers()))
            sb.append("private ");
    }

    /**
     * Prints result type and name of method in {@code StringBuilder}.
     * @param sb the {@code StringBuilder} in which will be stored result
     * @param method the method for which it is necessary to generate result type and name
     */
    private void printHeader(StringBuilder sb, Method method) {
        Type genRetType = method.getGenericReturnType();
        sb.append(genRetType.getTypeName()).append(' ');
        sb.append(method.getName());
    }

    /**
     * Prints parameters of method or constructor in {@code StringBuilder}
     * @param sb the {@code StringBuilder} in which will be stored result
     * @param exec method or constructor for which it is necessary print parameters
     */
    private void printParameters(StringBuilder sb, Executable exec) {
        int types = exec.getGenericParameterTypes().length;
        Type[] params = exec.getGenericParameterTypes();
        String[] typesArr = new String[types];
        for (int i = 0; i < types; ++i) {
            typesArr[i] = params[i].getTypeName();
        }
        printParameters(sb, typesArr);
    }

    /**
     * Prints exceptions of method or constructor in {@code StringBuilder}
     * @param sb the {@code StringBuilder} in which will be stored result
     * @param exec method or constructor for which it is necessary print exceptions
     */
    private void printExceptions(StringBuilder sb, Executable exec) {
        Type[] exceptions = exec.getGenericExceptionTypes();
        if (exceptions.length > 0) {
            sb.append(" throws ");
            for (int k = 0; k < exceptions.length; k++) {
                sb.append(((Class) exceptions[k]).getName());
                if (k + 1 != exceptions.length) {
                    sb.append(',');
                }
            }
        }
    }

    /**
     * Checks whether or not method {@code b} is implementation of method {@code a}.
     * @param a the first method
     * @param b the second method
     * @return {@code true} if method {@code b} is implementation of method {@code a}
     */
    public static boolean bIsImplementationOfA(Method a, Method b) {
        int aMod = a.getModifiers();
        if (Modifier.isAbstract(aMod) &&
                a.getName().equals(b.getName()) &&
                a.getReturnType().equals(b.getReturnType())) {

            Class[] parameterTypesA = a.getParameterTypes();
            Class[] parameterTypesB = b.getParameterTypes();
            if (parameterTypesA.length != parameterTypesB.length) {
                return false;
            }
            for (int i = 0; i < parameterTypesA.length; ++i)
                if (parameterTypesA[i] != parameterTypesB[i]) {
                    return false;
                }
            return a.getDeclaringClass().isAssignableFrom(b.getDeclaringClass());
        }
        return false;
    }

    /**
     * Gets all unimplemented abstract methods of class.
     * Probably some methods will repeat.
     * @param c the class, from which necessary gets unimplemented methods.
     * @param methods the container for in which all unimplemented methods will be added
     */
    private void getNotImplementedMethods(Class c, List<Method> methods) {
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

    /**
     * Gets all unimplemented abstract methods of implemented class and remove repetitions.
     * @return list of unimplemented abstract methods
     */
    public List<Method> getNotImplementedMethods() {
        List<Method> methods = new ArrayList<>();
        getNotImplementedMethods(baseClass, methods);
        ArrayList<Method> publics = new ArrayList<Method>();

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
        return publics;
    }

    /**
     * Checks whether or not method {@code m1} and method {@code m2} have equal names and types.
     * @param m1 first method
     * @param m2 second method
     * @return {@code true} if method equal
     */
    private boolean methodEquals(Method m1, Method m2) {
        return toSimpleStr(m1).equals(toSimpleStr(m2));
    }

    /**
     * Generates simple declaration of method, which will look like T0 name(T1, T2, ...).
     * @param method the method for which it is necessary to generate simple declaration
     * @return simple declaration of method
     */
    private String toSimpleStr(Method method) {
        StringBuilder sb = new StringBuilder();
        printHeader(sb, method);
        printParameters(sb, method);
        return sb.toString();
    }

    /**
     * Adds {@code method} in list if it is abstract or remove all method from list, which {@code method} implement.
     * @param methods list of abstract methods of class
     * @param method next abstract method
     */
    private void safeAdd(List<Method> methods, Method method) {
        int mod = method.getModifiers();
        if (Modifier.isAbstract(mod)) {
            methods.add(method);
            return;
        }
        for (Iterator<Method> it = methods.iterator(); it.hasNext(); ) {
            Method cur = it.next();
            if (bIsImplementationOfA(cur, method)) {
                it.remove();
            }
        }
    }

    /**
     * Default values for types.
     */
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
        defaults.put(byte.class, "0");
        DEFAULT_VALUES = Collections.unmodifiableMap(defaults);
    }

    /**
     * Find default value for type.
     * @param type the required type
     * @return default value for type
     */
    private static String getDefaultValue(Class<?> type) {
        return DEFAULT_VALUES.get(type);
    }

    /**
     * Enry point in the program.
     * @param args command lines arguments
     * @throws ClassNotFoundException if it wasn't possible load class
     * @throws ImplerException if it wasn't possible implement class
     */
    public static void main(String[] args) throws ClassNotFoundException, ImplerException {
        Class c = Class.forName(args[0]);
        if (args.length == 1) {
            try (
                Writer wr = new BufferedWriter(new FileWriter(c.getSimpleName() + "Impl.java")))
            {
                new Implementor(c).implement(wr);
            } catch (IOException e){
                System.out.println("exception file");
            }
        } else if (args.length == 2) {
            if (!args[1].endsWith(".jar")) {
                throw new IllegalArgumentException("Expected .jar file!");
            }
            new Implementor(c).implementJar(c, new File(args[1]));
        }
    }

    private static void compile(String file) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assert.assertNotNull("Could not find java compiler, include tools.jar to classpath", compiler);
        ArrayList<String> args = new ArrayList<>();
        args.add(file);
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));
        final int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        Assert.assertEquals("Compiler exit code", 0, exitCode);
    }

    private static void clean(final File file) {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File child : files) {
                    clean(child);
                }
            }
        }
        file.delete();
    }
}

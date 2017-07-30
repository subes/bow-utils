/*
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-5-3. For license
 * information see the LICENSE file in the root folder of this repository.
 */

package be.bagofwords.exec;

import be.bagofwords.logging.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RemoteObjectClassLoader extends ClassLoader {

    private final File compileDir;
    private final RemoteLogFactory remoteLogFactory;

    public RemoteObjectClassLoader(PackedRemoteObject packedRemoteObject, ClassLoader parentClassLoader, RemoteLogFactory remoteLogFactory) {
        super(parentClassLoader);
        this.remoteLogFactory = remoteLogFactory;
        try {
            this.compileDir = Files.createTempDirectory("java_compile").toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary root", e);
        }
        compileFiles(packedRemoteObject);
    }

    public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        if (!name.startsWith("java") && !name.startsWith("sun") && !name.startsWith("org.apache.log4j")) {
            String classPath = name.replaceAll("\\.", "/") + ".class";

            //First see if it a just compiled file
            File classFile = new File(compileDir, classPath);
            if (classFile.exists()) {
                try {
                    byte[] classBytes = FileUtils.readFileToByteArray(classFile);
                    System.out.println("Loaded class " + name);
                    return defineClass(name, classBytes, 0, classBytes.length);
                } catch (IOException exp) {
                    Log.w("Failed to read file " + classFile, exp);
                }
            }

            //Next, treat the slf4j StaticLoggerBinder specially if the remote log factory is set
            boolean candidateForLoading = name.startsWith("be.bagofwords.logging") || name.startsWith("org.slf4j");
            candidateForLoading &= !name.equals("org.slf4j.ILoggerFactory");
            candidateForLoading &= !name.equals("org.slf4j.Logger");
            candidateForLoading &= !name.equals("be.bagofwords.exec.RemoteLogFactory");
            if (remoteLogFactory != null && candidateForLoading) {
                InputStream is;
                boolean isStaticLogBinder = "org.slf4j.impl.StaticLoggerBinder".equals(name);
                if (isStaticLogBinder) {
                    is = getResourceAsStream("StaticLoggerBinder.class");
                } else {
                    is = getResourceAsStream(classPath);
                }
                if (is != null) {
                    try {
                        byte[] classBytes = IOUtils.toByteArray(is);
                        Class aClass = defineClass(name, classBytes, 0, classBytes.length);
                        if (isStaticLogBinder) {
                            Method setRemoteLogFactoryMethod = aClass.getMethod("setRemoteLogFactory", RemoteLogFactory.class);
                            setRemoteLogFactoryMethod.invoke(null, remoteLogFactory);
                            System.out.println("Loaded class " + name + " and did set remote log factory");
                        } else {
                            System.out.println("Loaded class " + name);
                        }
                        return aClass;
                    } catch (IllegalAccessException | InvocationTargetException | IOException | NoSuchMethodException e) {
                        throw new RuntimeException("Failed to set remote log factory", e);
                    }
                } else {
                    Log.w("Failed to load " + name + ". Do we have a file StaticLoggerBinder.class in the resources?");
                }
            }

            // //Try to read the class as a resource (this way, we initialise the class again, and avoid reusing classes as much as possible)
            // //This is for instance necessary to be able to set a different logger instance for RemoteApplicationExec objects
            // InputStream is = getResourceAsStream(classPath);
            // if (is != null) {
            //     try {
            //         byte[] classBytes = IOUtils.toByteArray(is);
            //         System.out.println("Loaded class " + name);
            //         return defineClass(name, classBytes, 0, classBytes.length);
            //     } catch (IOException exp) {
            //         Log.w("Failed to read resource " + name, exp);
            //     }
            // }
        }

        System.out.println("NOT loading class " + name);
        return super.loadClass(name, resolve);
    }

    private void compileFiles(PackedRemoteObject packedRemoteObject) {
        try {
            List<File> sourceFiles = new ArrayList<>();
            for (String className : packedRemoteObject.classSources.keySet()) {
                String filePathNoExtension = getFilePathNoExtension(className);
                File sourceFile = new File(compileDir, filePathNoExtension + ".java");
                File parentDir = sourceFile.getParentFile();
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    throw new RuntimeException("Could not create directory " + parentDir.getAbsolutePath());
                }
                FileUtils.write(sourceFile, packedRemoteObject.classSources.get(className), StandardCharsets.UTF_8);
                sourceFiles.add(sourceFile);
            }
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            List<String> optionList = new ArrayList<>();
            optionList.add("-classpath");
            optionList.add(System.getProperty("java.class.path"));
            optionList.add("-proc:none");
            Writer writer = new StringWriter();

            Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
            JavaCompiler.CompilationTask task = compiler.getTask(writer, fileManager, diagnostics, optionList, null, compilationUnit);
            boolean success = task.call();
            writer.close();
            if (!success) {
                StringBuilder errMessage = new StringBuilder();
                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    errMessage.append(diagnostic);
                    errMessage.append("\n");
                }
                throw new RuntimeException("Failed to compile source files " + writer.toString() + " " + errMessage.toString().trim());
            }
        } catch (IOException exp) {
            throw new RuntimeException("Failed to compile source files", exp);
        }
    }

    private String getFilePathNoExtension(String className) {
        return className.replaceAll("\\.", File.separatorChar + "");
    }
}
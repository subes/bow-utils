/*
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-5-3. For license
 * information see the LICENSE file in the root folder of this repository.
 */

package be.bagofwords.exec;

import org.apache.commons.io.FileUtils;

import javax.tools.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RemoteExecClassLoader extends ClassLoader {

    private final File compileDir;
    private URLClassLoader actualClassLoader;

    public RemoteExecClassLoader(PackedRemoteExec packedRemoteExec, ClassLoader parentClassLoader) {
        super(parentClassLoader);
        try {
            this.compileDir = Files.createTempDirectory("java_compile").toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary root", e);
        }
        compileFiles(packedRemoteExec);
        try {
            actualClassLoader = new URLClassLoader(new URL[]{compileDir.toURI().toURL()});
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unexpected error", e); //Should not occur
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        throw new RuntimeException("Loading resources is currently not supported");
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        File compiledFile = new File(compileDir, getFilePathNoExtension(name) + ".class");
        if (compiledFile.exists()) {
            return actualClassLoader.loadClass(name);
        } else {
            return super.loadClass(name);
        }
    }

    private void compileFiles(PackedRemoteExec packedRemoteExec) {
        try {
            List<File> sourceFiles = new ArrayList<>();
            for (String className : packedRemoteExec.classSources.keySet()) {
                String filePathNoExtension = getFilePathNoExtension(className);
                File sourceFile = new File(compileDir, filePathNoExtension + ".java");
                File parentDir = sourceFile.getParentFile();
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    throw new RuntimeException("Could not create directory " + parentDir.getAbsolutePath());
                }
                FileUtils.write(sourceFile, packedRemoteExec.classSources.get(className), StandardCharsets.UTF_8);
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
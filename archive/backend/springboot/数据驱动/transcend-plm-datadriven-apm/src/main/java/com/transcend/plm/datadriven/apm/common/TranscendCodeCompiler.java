package com.transcend.plm.datadriven.apm.common;

import lombok.extern.slf4j.Slf4j;

import javax.tools.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description: 编译器(用一句话描述该文件做什么)
 *
 * @author sgx
 * date 2024/6/17 9:09
 * @version V1.0
 */

@Slf4j
public class TranscendCodeCompiler {

    /**
     * java文件后缀
     */
    private static final String JAVA_FILE_SUFFIX = ".java";


    /**
     * class文件后缀
     */
    private static final String CLASS_FILE_SUFFIX = ".class";


    /**
     * 基础目录结构
     */
    private static final String BASIC_DIRECTORY_STRUCTURE = "/transcend-plm-extend/src/main/java/";

    /**
     * 基础包结构
     */
    private static final String BASIC_PACKAGE_STRUCTURE = "com.transcend.plm.datadriven.apm.extend";

    /**
     * 基础目录结构
     */
    private static final String FILE_PREFIX = "file:/";

    /**
     * 文件分隔符
     */
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");



    /**
     * 执行代码
     * @param fileName 文件名
     * @param methodName 方法名
     * @param args  参数
     * @return
     */
    public static Object executionMethod(String fileName, String methodName, Object... args) throws Exception {
        String currentDir = System.getProperty("user.dir")  ;
        String fileParentPath = FILE_PREFIX+currentDir + BASIC_DIRECTORY_STRUCTURE ;
        // 使用URLClassLoader加载class到内存
        URL[] urls = new URL[]{new URL(fileParentPath)};
        URLClassLoader cLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        String className = "";
        if (fileName.endsWith(CLASS_FILE_SUFFIX)) {
            fileName = fileName.substring(0, fileName.length() - 6);
        }else if (fileName.endsWith(JAVA_FILE_SUFFIX)) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        if (fileName.contains(FILE_SEPARATOR)) {
            className = fileName.replace(FILE_SEPARATOR.charAt(0), '.') ;
        }else if (fileName.contains("/")) {
            className = fileName.replace('/', '.') ;
        }else if (!fileName.contains(".") && !fileName.contains("/") && !fileName.contains(FILE_SEPARATOR)) {
            className = BASIC_PACKAGE_STRUCTURE+"."+ fileName;
        }
        else {
            className = fileName;
        }
        // 加载类
        Class<?> c = cLoader.loadClass(className);
        // 关闭类加载器
        cLoader.close();

        // 利用class创建实例，反射执行方法
        Object obj = c.newInstance();
        Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        // 获取类中的方法
        Method method = c.getMethod(methodName,parameterTypes);
        // 执行方法
        Object result = method.invoke(obj, args);
        Class<?> returnType = method.getReturnType();
        return result;
    }



    /**
     * 编译源代码
     *
     * @param sourceCode 源代码
     * @return 编译结果
     * @throws Exception 异常
     */
    public static Boolean compile(String sourceCode){
        try {
            //创建源文件
            String currentDir = System.getProperty("user.dir")  ;
            String packageName = extractPackageName(sourceCode);
            if ( packageName == null) {
                packageName = BASIC_PACKAGE_STRUCTURE;
                sourceCode = "package " + packageName + ";\n" + sourceCode;
            }
            // package后面拼接一个.
            packageName = packageName + ".";
            String packagePath = packageToPath(packageName);
            String className = extractClassName(sourceCode);

            // 源文件路径和名称
            String filename = currentDir +BASIC_DIRECTORY_STRUCTURE+packagePath+className+JAVA_FILE_SUFFIX;
            File file = new File(filename);
            // 确保源文件所在的目录存在
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                boolean created = fileParent.mkdirs();
                if (!created) {
                    System.out.println("目录创建失败，路径可能不正确或没有权限");
                }
            }
            // 使用 try-with-resources 语句自动关闭资源
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // 写入内容，这将创建文件或覆盖已存在的文件
                writer.write(sourceCode);
                log.info("文件已被创建或覆盖，内容写入成功。");
            } catch (IOException e) {
                e.printStackTrace();
                log.error("文件写入失败", e.getMessage());
            }

            // 使用JavaCompiler 编译java文件
            // 获取系统Java编译器
            JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
            // 建立DiagnosticCollector对象
            DiagnosticCollector diagnostics = new DiagnosticCollector();
            // 获取标准文件管理器
            StandardJavaFileManager fileManager = jc.getStandardFileManager(diagnostics, null, null);
            // 获取要编译的文件对象
            Iterable fileObjects = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(file));
            // 创建编译任务
            JavaCompiler.CompilationTask cTask   = jc.getTask(null, fileManager, diagnostics, null, null, fileObjects);
            // 执行编译任务
            boolean success = cTask.call();
            // 关闭文件管理器
            fileManager.close();
            log.info("编译结果：" + (success ? "成功" : "失败"));
            return success;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 将包名转换为路径
     *
     * @param packageName 包名
     * @return 路径
     */
    public static String packageToPath(String packageName) {
        // 将点（.）替换为文件系统的路径分隔符
        return packageName.replace('.', FILE_SEPARATOR.charAt(0));
    }



    /**
     * 从源代码中提取包名
     *
     * @param sourceCode 源代码
     * @return 包名
     */
    public static String extractPackageName(String sourceCode) {
        // 正则表达式用于匹配包声明
        String patStr = "package\\s+([\\w\\.]+)\\s*;";
        Pattern pattern = Pattern.compile(patStr);
        Matcher matcher = pattern.matcher(sourceCode);
        // 检查是否有匹配的包声明
        if (matcher.find()) {
            // 返回第一个捕获组，即包名
            return matcher.group(1);
        }
        // 如果没有找到匹配的包声明，返回默认包名
        return null;
    }

    /**
     * 从源代码中提取类名
     *
     * @param sourceCode 源代码
     * @return 类名
     */
    public static String extractClassName(String sourceCode) {
        // 正则表达式用于匹配类定义
        String parStr = "class\\s+([\\w]+)\\s*\\{";
        Pattern pattern = Pattern.compile(parStr);
        Matcher matcher = pattern.matcher(sourceCode);

        // 检查是否有匹配的类定义
        if (matcher.find()) {
            // 返回第一个捕获组，即类名
            return matcher.group(1);
        }
        // 如果没有找到匹配的类定义，返回null
        return null;
    }

    public static void main(String[] args) throws Exception {

        // 源代码字符串
        String sourceCode = "package com.transcend.plm.datadriven.apm.extt; public class HelloWorld { public void sayHello(String name) { System.out.println(\"Hello \"+name+\", World\"); } }";
//        compile(sourceCode);
//        executionMethod("com.transcend.plm.datadriven.apm.extt.HelloWorld","sayHello","zhangsan");
//        executionMethod("com.transcend.plm.datadriven.apm.extend.HelloWorld.java","sayHello");
//        executionMethod("com.transcend.plm.datadriven.apm.extend.HelloWorld.class","sayHello");
        executionMethod("HelloWorld","sayHello");
        executionMethod("com\\transcend\\plm\\datadriven\\apm\\extend\\HelloWorld","sayHello");
    }

    static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}

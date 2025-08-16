package com.transcend.plm.datadriven.apm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * description: maven编译器(用一句话描述该文件做什么)
 *
 * @author sgx
 * date 2024/6/17 9:27
 * @version V1.0
 */
@Slf4j
public class TranscendMavenInvoker {

    /**
     * Maven 所在路径，如果Maven在系统PATH变量中，可以直接使用"mvn"
     */
    @Value("${mvn.path: mvn}")
    private static String mavenCommand = "D:\\apache-maven-3.9.6\\bin\\mvn.cmd";

    /**
     * Maven 命令，这里使用"clean install"来清理之前的构建并进行安装
     */
    @Value("${mvn.goals: clean}")
    private static String goals = "clean -Dmaven.test.skip=true";


    public static boolean mvnCompile(String projectDirectory) {
        BufferedReader reader = null;
        Process process = null;
        try {
            // 进入项目目录
            System.out.println("Changing directory to: " + projectDirectory);
            // 确保目录存在
            new File(projectDirectory).mkdirs();
            System.setProperty("user.dir", projectDirectory);
            System.setProperty("file.encoding", "UTF-8");
            String[] command = (mavenCommand + " " + goals).split(" ");
            // 创建 ProcessBuilder 对象
            ProcessBuilder processBuilder = new ProcessBuilder(command);

            // 重定向错误流到输出流
            processBuilder.redirectErrorStream(true);

            // 启动 Maven 进程
            System.out.println("Executing Maven command: " + mavenCommand + " " + goals);
            process = processBuilder.start();
            // 读取 Maven 输出
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // 等待 Maven 进程结束
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Maven build completed successfully.");
                return true;
            } else {
                System.out.println("Maven build failed with exit code: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流和进程
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("关闭标准输出流失败", e);
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // 项目路径，这里需要替换为你的Maven项目的根目录路径
        String projectDirectory = "E:\\transcend-plm-datadriven";
        mvnCompile(projectDirectory);

    }
}

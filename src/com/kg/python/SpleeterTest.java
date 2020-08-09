package com.kg.python;//import org.gradle.tooling.BuildLauncher;
//import org.gradle.tooling.GradleConnector;
//import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

public class SpleeterTest {
    public static void main(String args[]) {
    /*    GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File("."));
        ProjectConnection connection = connector.connect();
        BuildLauncher build = connection.newBuild();
        build.forTasks("runSpleeter");
        build.addJvmArguments("-Dspleeter1="+"C:/Users/Paul/Documents/horde/eadc0b07-0d3e-4b6e-8d6a-245b9a31b366001.wav");
        build.addJvmArguments("-Dspleeter2="+"spleeter");
        build.setStandardOutput(System.out);
        build.run();
        connection.close();
    */
        PythonPIP.installPIP();
        File file = new File("C:/Users/Paul/Documents/horde/eadc0b07-0d3e-4b6e-8d6a-245b9a31b366001.wav");
        SpleeterTest.spleet(file,4);
    }

    public static void spleet(File file,int stem) {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        String pro = "python -m spleeter separate -p spleeter:"+stem+"stems -i " + file.getAbsolutePath() + " -o spleeter";
        if (isWindows) {
            builder.command("cmd.exe", "/c", pro);
        } else {
            builder.command("sh", "-c", pro);
        }
        builder.directory(new File(System.getProperty("user.dir")));
        builder.inheritIO();
        Process process = null;
        try {
            for (String c : builder.command()) {
                System.out.print(c + " ");
            }
            System.out.println();
            process = builder.start();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

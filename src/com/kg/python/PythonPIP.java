package com.kg.python;//import org.gradle.tooling.BuildLauncher;
//import org.gradle.tooling.GradleConnector;
//import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.io.IOException;

public class PythonPIP {
    public static void installPIP() {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        String pro = "python " + new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator + "setup.py";
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

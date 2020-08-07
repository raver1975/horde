package com.kg;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;

public class SpleeterTest {
    public static void main(String args[]) {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File("."));
        ProjectConnection connection = connector.connect();
        BuildLauncher build = connection.newBuild();
        build.forTasks(new String[]{"runSpleeter"});
        build.setStandardOutput(System.out);
        build.run();

        connection.close();
    }
}

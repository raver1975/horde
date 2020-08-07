package com.kg;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.util.HashMap;

public class SpleeterTest {
    public static void main(String args[]) {
//        System.setProperty("spleeter","separate -h");
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File("."));
        ProjectConnection connection = connector.connect();
        BuildLauncher build = connection.newBuild();
        build.forTasks("runSpleeter");
        build.addJvmArguments("-Dspleeter1="+"C:/Users/Paul/Documents/horde/eadc0b07-0d3e-4b6e-8d6a-245b9a31b366001.wav");
        build.addJvmArguments("-Dspleeter2="+"spleeter");
//        HashMap<String,String> hm=new HashMap(System.getenv());
//        build.setEnvironmentVariables(hm);
        build.setStandardOutput(System.out);
        build.run();

        connection.close();
    }
}

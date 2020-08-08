//import org.gradle.tooling.BuildLauncher;
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
        SpleeterTest st=new SpleeterTest();
        st.installSpleeter();
        st.spleet();
    }

    public void spleet(){
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        String pro="python -m spleeter separate -p spleeter:2stems -i C:/Users/Paul/Documents/horde/eadc0b07-0d3e-4b6e-8d6a-245b9a31b366001.wav -o spleeter";
        if (isWindows) {
            builder.command("cmd.exe", "/c", pro);
        } else {
            builder.command("sh", "-c", pro);
        }
        builder.directory(new File(System.getProperty("user.home")));
        Process process = null;
        try {
            for (String c:builder.command()){
                System.out.print(c+" ");
            }
            System.out.println();
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert exitCode == 0;
    }

    public void installSpleeter(){
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        String pro="python "+new File("").getAbsolutePath()+File.separator+"setup.py";
        if (isWindows) {
            builder.command("cmd.exe", "/c", pro);
        } else {
            builder.command("sh", "-c", pro);
        }
        builder.directory(new File(System.getProperty("user.home")));
        Process process = null;
        try {
            for (String c:builder.command()){
                System.out.print(c+" ");
            }
            System.out.println();
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(),System.out::println);

        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert exitCode == 0;
    }
}

package com.kg.python;//import org.gradle.tooling.BuildLauncher;
//import org.gradle.tooling.GradleConnector;
//import org.gradle.tooling.ProjectConnection;

import com.echonest.api.v4.TrackAnalysis;
import com.kg.wub.AudioObject;
import com.kg.wub.system.SpotifyUtils;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static com.kg.python.SpotifyDLTest.STEMS.*;
import static com.kg.python.SpotifyDLTest.STEMS.STEM2;

public class SpotifyDLTest {
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
        String spotifyId="";
        String fileName="https://open.spotify.com/track/7mFBGqpnvIT9SyF1WCD9sm?si=I0VviOI8T0u23344rom5_Q";
        TrackAnalysis ta=null;

        if (fileName.contains("spotify:track:") || fileName.contains("https://open.spotify.com/track/")) {
            if (fileName.lastIndexOf("/") > -1) spotifyId = fileName.substring(fileName.lastIndexOf("/") + 1);
            else if (fileName.lastIndexOf(":") > -1) spotifyId = fileName.substring(fileName.lastIndexOf(":") + 1);
            System.out.println("spotifyID=" + spotifyId);
            try {
                ta = SpotifyUtils.getAnalysis(spotifyId);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        List<File> files=SpotifyDLTest.spotifyAndSpleeter(fileName, new File(System.getProperty("user.dir") + File.separator + URLEncoder.encode(spotifyId)+".mp3"), STEM5);

        for (File f:files){
            AudioObject.factory(f.getAbsolutePath(),ta);
        }
    }

    public static void spotify(String trackurl, File outputFile) {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        String pro = "python -m spotdl.command_line.__main__ --overwrite force --song " + trackurl + " -f " + outputFile.getAbsolutePath();
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

    public static enum STEMS {
        STEM0, STEM2, STEM4, STEM5;
    }

    public static List<File> spotifyAndSpleeter(String trackURL, File outputFile, STEMS stem) {
        ArrayList<File> outfiles = new ArrayList<>();
        SpotifyDLTest.spotify(trackURL, outputFile);
        outfiles = new ArrayList<>();
        outfiles.add(outputFile);
        String base = System.getProperty("user.dir") + File.separator + "spleeter" + File.separator + outputFile.getName().replaceAll("^.*?(([^/\\\\\\.]+))\\.[^\\.]+$", "$1") + File.separator;
        switch (stem) {
            case STEM0:
                break;
            case STEM2:
                SpleeterTest.spleet(outputFile, 2);
                outfiles.add(new File(base + "vocals.wav"));
                outfiles.add(new File(base + "accompaniment.wav"));
                break;
            case STEM4:
                SpleeterTest.spleet(outputFile, 4);
                outfiles.add(new File(base + "vocals.wav"));
                outfiles.add(new File(base + "bass.wav"));
                outfiles.add(new File(base + "other.wav"));
                outfiles.add(new File(base + "drums.wav"));
                break;
            case STEM5:
                SpleeterTest.spleet(outputFile, 5);
                outfiles.add(new File(base + "vocals.wav"));
                outfiles.add(new File(base + "piano.wav"));
                outfiles.add(new File(base + "bass.wav"));
                outfiles.add(new File(base + "other.wav"));
                outfiles.add(new File(base + "drums.wav"));
                break;
            default:
                throw new IllegalStateException("Unexpected value: stem " + stem);
        }
        for (File f : outfiles) {
            System.out.println(f.exists() + "\t" + f.getAbsolutePath());
        }
        return outfiles;
    }
}

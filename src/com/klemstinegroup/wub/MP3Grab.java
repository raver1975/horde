package com.klemstinegroup.wub;

import com.echonest.api.v4.TrackAnalysis;
import com.klemstinegroup.wub.system.SpotifyUtils;
import com.wrapper.spotify.models.Track;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Created by Paul on 2/25/2017.
 */
public class MP3Grab {

    private static String queryOverride = null;
//    public static String spotifyId = "spotify:track:5ghIJDpPoe3CfHMGu71E6T";
//    public static String spotifyId = "spotify:track:6z0zyXMTA0ans4OoTAO2Bm";

//    static {
//        spotifyId = spotifyId.replace("spotify:track:", "");
//    }

    private boolean firstSongIsLoadedYet;


    public MP3Grab() throws IOException, ParseException {
    }

    public static void main(String[] args) {
        final String[] spotifyId = new String[1];
        if (args.length == 0) {
            System.out.println("Please enter a spotify track url or wub file:");
            Scanner scanner = new Scanner(System.in);
            FutureTask<String> readNextLine = new FutureTask<String>(() -> {
                return scanner.nextLine();
            });

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.execute(readNextLine);

            String s = null;
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        spotifyId[0] = SpotifyUtils.getRandomID();
                    }
                }).start();
                s = readNextLine.get(15000, TimeUnit.MILLISECONDS);


            } catch (TimeoutException e) {
                // handle time out
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }


            if (s != null && new File(s).exists()) {
                AudioObject au = AudioObject.factory(s);
                return;
            }

            if (s != null && s.length() > 0) spotifyId[0] = s;
        }
        if (args.length == 1) {
            spotifyId[0] = args[0];
        }

        System.out.println(spotifyId[0]);
        try {
            new MP3Grab().grab(spotifyId[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AudioObject grab(String spotifyId) throws Exception {
        spotifyId = spotifyId.replace("spotify:track:", "");
        Track track = SpotifyUtils.getTrack(spotifyId);
        System.out.println("got track info");
        JSONObject js1 = null;
        if (queryOverride != null && !queryOverride.isEmpty()) {
            js1 = SpotifyUtils.getDownloadList(queryOverride);
        } else {
            js1 = SpotifyUtils.getDownloadList(track.getArtists().get(0).getName() + " " + track.getName());
        }
        JSONArray js2 = (JSONArray) js1.get("data");
        System.out.println(js2.toString());

        TrackAnalysis ta = SpotifyUtils.getAnalysis(spotifyId);

        System.out.println("Searching for duration " + ta.getDuration());
        for (int i = 0; i < js2.size(); i++) {
            try {
                JSONObject data = (JSONObject) js2.get(i);
                long duration = (Long) data.get("duration");
                if (Math.abs((int) duration - ta.getDuration()) < 2d) {
                    System.out.println("*****" + duration + "\t" + data.toString());

                    String downloadUrl = (String) data.get("download");
                    downloadUrl = downloadUrl.replaceAll("127.0.0.1", "127.0.0.1:8000");
                    System.out.println("Downloading song from: " + downloadUrl);

                    URLConnection conn = SpotifyUtils.getConnection(new URL(downloadUrl));
                    InputStream is = conn.getInputStream();
                    String outputFile = track.getArtists().get(0).getName() + "-" + track.getName() + "-" + i + ".mp3";

                    File file = new File(outputFile);
                    if (!file.exists()) {
                        OutputStream outstream = new FileOutputStream(file);
                        byte[] buffer = new byte[4096];
                        int len;
                        int tot = 0;
                        int cnt = 0;
                        int rows = 0;
                        System.out.print(tot + ":");
                        while ((len = is.read(buffer)) > 0) {
                            System.out.print("*");
                            if (cnt++ > 40) {
                                tot++;
                                System.out.println();

                                System.out.print(tot + ":");
                                cnt = 0;

                            }
                            outstream.write(buffer, 0, len);
                        }
                        outstream.close();

                    }
                    System.out.println("\nDone!");
                    if (!firstSongIsLoadedYet) {
                        firstSongIsLoadedYet = true;
                        System.out.println(outputFile);
                        AudioObject au = AudioObject.factory(outputFile, ta);
                        return au;
                    }
                } else {
                    System.out.println(duration + "\t" + data.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}

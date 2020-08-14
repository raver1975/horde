package com.kg.wub;

import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.TrackAnalysis;
import com.kg.TheHorde;
import com.kg.python.SpotifyDLTest;
import com.kg.wub.system.*;
import com.sun.media.sound.WaveFileWriter;
import org.json.simple.parser.ParseException;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.Queue;
import java.util.*;

import static com.kg.python.SpotifyDLTest.STEMS.*;

public class AudioObject implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 379377752113122689L;
    transient int filecount = 0;
    public byte[] data;
    public File file;
    public TrackAnalysis analysis;
    public static String spotifyId;

    public transient MusicCanvas mc;
    public transient SourceDataLine line;
    public transient Queue<Interval> queue;
    public transient int position = 0;
    public transient Interval currentlyPlaying;
    public transient boolean breakPlay;
    public transient boolean pause = false;
    public transient boolean loop = false;
    public transient HashMap<String, Interval> midiMap;
//	public static double tolerance = .2d;

    public static final int resolution = 16;
    public static final int channels = 2;
    public static final int frameSize = channels * resolution / 8;
    public static final int sampleRate = 44100;
    public static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels, frameSize, sampleRate, false);
    static final int bufferSize = 8192;
    public static String key = null;

    public AudioObject(Song song, File file) {
        this(song.data, song.analysis, file);
    }

    public static AudioObject factory() {
        JFileChooser chooser = new JFileChooser(CentralCommand.lastDirectory);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Wub", "wub", "play");
        chooser.setFileFilter(filter);
//        chooser.setSelectedFile(new File("spotify:ID or WUB file"));
        chooser.setSelectedFile(new File("spotify:track:3rPFO1SnHNm0PKIrC5ciA3"));
        int returnVal = chooser.showOpenDialog(new JFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (chooser.getSelectedFile().toString().contains("spotify:track:") || chooser.getSelectedFile().toString().contains("https://open.spotify.com/track/")) {
                return factory(chooser.getSelectedFile().toString(), null);

            }
            // System.out.println("You chose to open this file: " +
            CentralCommand.lastDirectory = chooser.getSelectedFile();
            if (chooser.getSelectedFile().getAbsolutePath().endsWith(".play")) {
                CentralCommand.loadPlay(chooser.getSelectedFile());
                return null;
            }
            TrackAnalysis ta = null;
//            try {
//                System.out.println("here!: spotifyId="+spotifyId);
//                ta = SpotifyUtils.getAnalysis(spotifyId);
//            } catch (IOException | ParseException e) {
//                e.printStackTrace();
//            }
            return factory(chooser.getSelectedFile().getAbsolutePath(), null);
        }
        return null;
    }

    public static AudioObject factory(String file) {
//
//        if (file.startsWith("spotify:track:")) {
//            try {
//                return new MP3Grab().grab(file);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        return factory(file, null);
    }

    public static AudioObject factory(String fileName, TrackAnalysis ta) {
        if (PlayingField.frame != null) {
            PlayingField.frame.setVisible(true);
        }
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
//            JFileChooser chooser = new JFileChooser(CentralCommand.lastDirectory);
//            FileNameExtensionFilter filter = new FileNameExtensionFilter("Audio", "mp3", "wav", "wub", "play");
//            chooser.setFileFilter(filter);
//            int returnVal = chooser.showOpenDialog(new JFrame());
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                 System.out.println("You chose to open this file: " +
//                CentralCommand.lastDirectory = chooser.getSelectedFile();
//                if (chooser.getSelectedFile().getAbsolutePath().endsWith(".play")) {
//                    CentralCommand.loadPlay(chooser.getSelectedFile());
//                    return null;
//                }
            File spotifyFile = new File(System.getProperty("user.dir") + File.separator + URLEncoder.encode(spotifyId) + ".mp3");
            List<File> spleets = SpotifyDLTest.spotifyAndSpleeter(fileName, spotifyFile, TheHorde.stem);
            for (File f : spleets) {
                factory(f.getAbsolutePath(), ta);
            }
            CentralCommand.pf.makeData();
            return null;
        }
//        }
        File file = new File(fileName);
        System.out.println(file.exists());

        File newFile = file;
        String extension = "";
        String filePrefix = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
            filePrefix = fileName.substring(0, i);
        }
        if (extension.equals("play")) {
            CentralCommand.lastDirectory = file;
            CentralCommand.loadPlay(file);
            return null;
        }
        if (extension.equals("wub")) {
            try {
                AudioObject au = (AudioObject) Serializer.load(newFile);
                double bpm = 120;
                if (TheHorde.output != null) {
                    bpm = TheHorde.output.getSequencers()[0].getBpm();

                    //Timestretch
                    AudioInterval ad = new AudioInterval(au.data);
                    AudioInterval[] ai = ad.getMono();
                    double bpmFactor = bpm / au.analysis.getTempo();
                    AudioUtils.timeStretch1(ai[0], bpmFactor);
                    AudioUtils.timeStretch1(ai[1], bpmFactor);
                    ad.makeStereo(ai);
                    au.data = ad.data;
                    au.analysis.timeStretch(bpmFactor);
                    //Timestretch
                }
                au.init(true);
                return au;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        AudioObject au = new AudioObject(file, ta);
        double bpm = 120;
        if (TheHorde.output != null) {
            bpm = TheHorde.output.getSequencers()[0].getBpm();

            //Timestretch
            AudioInterval ad = new AudioInterval(au.data);
            AudioInterval[] ai = ad.getMono();
            double bpmFactor = bpm / au.analysis.getTempo();
            AudioUtils.timeStretch1(ai[0], bpmFactor);
            AudioUtils.timeStretch1(ai[1], bpmFactor);
            ad.makeStereo(ai);
            au.data = ad.data;
//            au.analysis = new TrackAnalysis(au.analysis);
            au.analysis.timeStretch(bpmFactor);
            System.out.println("2bpm:" + au.analysis.getTempo());
            //Timestretch
        }
        au.init(true);
        try {
            if (!extension.equals("wub")) {
                newFile = new File(filePrefix + ".wub");
                System.out.println("saving to:" + newFile.getAbsolutePath());
            }

            Serializer.store(au, newFile);
        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CentralCommand.pf.makeData();
        return au;
    }

    public AudioObject(final File file, TrackAnalysis ta) {
        this.file = file;
        convert(file);
        JTextArea msgLabel;
        JProgressBar progressBar;
        final int MAXIMUM = 100;
        JPanel panel;

        progressBar = new JProgressBar(0, MAXIMUM);
        progressBar.setIndeterminate(true);
        msgLabel = new JTextArea(file.getName());
        msgLabel.setEditable(false);

        panel = new JPanel(new BorderLayout(5, 5));
        panel.add(msgLabel, BorderLayout.PAGE_START);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));

        final JDialog dialog = new JDialog();
        dialog.setTitle("Analyzing audio...");
        dialog.getContentPane().add(panel);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setSize(500, dialog.getHeight());
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setAlwaysOnTop(false);
        dialog.setVisible(true);
        msgLabel.setBackground(panel.getBackground());
        if (ta != null) {
            try {
                analysis = (TrackAnalysis) Serializer.deepclone(ta);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        dialog.dispose();
    }

    public AudioObject(byte[] by, TrackAnalysis fa, File file) {
        this.file = file;
        analysis = fa;
        data = by;
        init(true);
    }

    public void init(boolean addtoCentral) {
        midiMap = new HashMap<String, Interval>();
        queue = new LinkedList<Interval>();
        mc = new MusicCanvas(this);
        if (addtoCentral)
            CentralCommand.add(this);
        startPlaying();
    }

    private void startPlaying() {
        line = getLine();
        new Thread(new Runnable() {
            public void run() {
                top:
                while (true) {

                    // System.out.println(queue.size());
                    if (!queue.isEmpty()) {
                        Interval i = queue.poll();

                        currentlyPlaying = i;
                        int j = 0;
                        for (j = Math.max(0, i.startBytes); j <= i.endBytes - bufferSize && j < data.length - bufferSize; j += bufferSize) {
                            while (pause || breakPlay) {
                                if (breakPlay) {
                                    breakPlay = false;
                                    // if (loop)
                                    // queue.add(i);
                                    // queue.clear();
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    continue top;
                                }
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            position = j;
                            line.write(data, j, bufferSize);

                        }

                        if (j < i.endBytes && i.endBytes < data.length) {
                            position = j;
                            line.write(data, j, i.endBytes - j);
                            // line.drain();
                        }
                        if (loop)
                            queue.add(i);
                    } else

                        currentlyPlaying = null;
                    if (!mc.mouseDown)
                        mc.tempTimedEvent = null;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

	/*public TrackAnalysis echoNest(File file) {
		int cnt = 0;
		while (cnt < 5) {
			try {
				EchoNestAPI en = null;
				if (key != null)
					en = new EchoNestAPI(key);
				else
					en = new EchoNestAPI();
				Track track = en.uploadTrack(file);
				System.out.println(track);
				track.waitForAnalysis(30000);
				if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
					return track.getAnalysis();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return analysis;
	}*/

    public void convert(File soundFile) {
        AudioInputStream mp3InputStream = null;
        try {
            System.out.println(soundFile.getAbsolutePath());
            mp3InputStream = AudioSystem.getAudioInputStream(soundFile.getAbsoluteFile());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File temp = new File("temp.wav");
        mp3InputStream = AudioSystem.getAudioInputStream(new AudioFormat(mp3InputStream.getFormat().getSampleRate(), resolution, AudioObject.channels, true, false), mp3InputStream);
        try {
            AudioSystem.write(mp3InputStream, AudioFileFormat.Type.WAVE, temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // try {
        // data = Files.readAllBytes(temp.toPath());
        try {
            mp3InputStream = AudioSystem.getAudioInputStream(temp);
        } catch (UnsupportedAudioFileException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        mp3InputStream = AudioSystem.getAudioInputStream(AudioObject.audioFormat, mp3InputStream);

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            AudioSystem.write(mp3InputStream, AudioFileFormat.Type.WAVE, bo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        data = bo.toByteArray();

        try {
            mp3InputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        temp.delete();
    }

    public String getFileName() {
        if (file == null) return null;
        return file.getName();
    }

    public SourceDataLine getLine() {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, AudioObject.audioFormat);
        try {
            res = (SourceDataLine) AudioSystem.getLine(info);
            res.open(AudioObject.audioFormat);
            res.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void play(Interval i) {
        queue.add(i);
    }

    // public void sendMidi(String keyName, int vel) {
    // System.out.println(keyName + "\t" + vel);
    // Interval i = midiMap.get(keyName);
    // if (vel > 0) {
    // if (i == null) {
    // if (mc.hovering != null)
    // midiMap.put(keyName, mc.hovering);
    // } else {
    // // if (queue.size()>0){
    // // breakPlay = true;
    // // }
    // // while (breakPlay) {
    // // try {
    // // Thread.sleep(10);
    // // } catch (InterruptedException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // }
    // // }
    // // System.out.println("add");
    // queue.add(i);
    // }
    // }
    // // else {
    // // if (i != null) {
    // // if (i.equals(currentlyPlaying)) {
    // // breakPlay = true;
    // // }
    // // }
    // // }
    //
    // }

    // public void play(double start, double duration) {
    // int startInBytes = (int) (start * AudioObject.sampleRate *
    // AudioObject.frameSize) - (int) (start * AudioObject.sampleRate *
    // AudioObject.frameSize) % AudioObject.frameSize;
    // double lengthInFrames = duration * AudioObject.sampleRate;
    // int lengthInBytes = (int) (lengthInFrames * AudioObject.frameSize) -
    // (int) (lengthInFrames * AudioObject.frameSize) % AudioObject.frameSize;
    // queue.add(new Interval(startInBytes, Math.min(startInBytes +
    // lengthInBytes, data.length)));
    //
    // }


    public void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 2;
        byte tmp1;
        byte tmp2;

        while (j > i) {
            tmp1 = array[j];
            tmp2 = array[j + 1];
            array[j] = array[i];
            array[j + 1] = array[i + 1];
            array[i] = tmp1;
            array[i + 1] = tmp2;
            j -= 2;
            i += 2;
        }
    }

    public AudioObject createAudioObject() {
        boolean savePause = pause;
        pause = true;
        TrackAnalysis fa = new TrackAnalysis(null);
        fa.setTempo(analysis.getTempo());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LinkedList<Interval> ll = new LinkedList<Interval>();
        if (currentlyPlaying != null) {
            ll.add(currentlyPlaying);
        }
        ll.addAll(queue);
        int bytecnt = 0;
        if (ll.size() == 0 && mc.hovering != null)
            ll.add(mc.hovering);
        if (ll.size() == 0)
            return null;
        for (Interval i : ll) {
            i.newbytestart = bytecnt;
            if (i.lengthBytes + i.startBytes > data.length) {
                i.lengthBytes = data.length - i.startBytes;
            }
            baos.write(data, i.startBytes, i.lengthBytes);
            bytecnt += i.lengthBytes;

        }
        byte[] by = baos.toByteArray();
        fa.setDuration(convertByteToTime(by.length));
        //fa.setTempo(this.analysis.getTempo());
        Collections.sort(ll, new Comparator<Interval>() {

            @Override
            public int compare(Interval o1, Interval o2) {
                return Double.compare(o2.startBytes, o1.startBytes);
            }

        });

//        fa.getSections().add(new TimedEvent(0d, fa.getDuration(), 1d));

        for (Interval i : ll) {
            for (Segment e : analysis.getSegments()) {
                TimedEvent nt = intersects(i.te, e, i.newbytestart);
                if (nt != null) {
                    Segment f = null;
                    try {
                        f = (Segment) Serializer.deepclone(e);
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    f.start = nt.start;
                    f.duration = nt.duration;
                    fa.getSegments().add(f);
                }
            }

            for (TimedEvent e : analysis.getSections()) {
                TimedEvent nt = intersects(i.te, e, i.newbytestart);
                if (nt != null) {
                    fa.getSections().add(nt);
                }
            }

            for (TimedEvent e : analysis.getBars()) {
                TimedEvent nt = intersects(i.te, e, i.newbytestart);
                if (nt != null) {
                    fa.getBars().add(nt);
                }
            }

            for (TimedEvent e : analysis.getBeats()) {
                TimedEvent nt = intersects(i.te, e, i.newbytestart);
                if (nt != null) {
                    fa.getBeats().add(nt);
                }
            }

            for (TimedEvent e : analysis.getTatums()) {
                TimedEvent nt = intersects(i.te, e, i.newbytestart);
                if (nt != null) {
                    fa.getTatums().add(nt);
                }
            }

            Comparator<TimedEvent> compare = new Comparator<TimedEvent>() {
                @Override
                public int compare(TimedEvent o1, TimedEvent o2) {
                    return Double.compare(o1.getStart(), o2.getStart());
                }

            };
            Collections.sort(fa.getSegments(), compare);
            Collections.sort(fa.getSections(), compare);
            Collections.sort(fa.getBars(), compare);
            Collections.sort(fa.getBeats(), compare);
            Collections.sort(fa.getTatums(), compare);
        }

        if (file == null) file = new File(UUID.randomUUID().toString() + ".wav");
        String fileName = file.getAbsolutePath();
        String extension = "";
        String filePrefix = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
            filePrefix = fileName.substring(0, i);
        }
        String filePrefix1 = null;
        do {
            filecount++;
            filePrefix1 = filePrefix + String.format("_%03d", filecount);
        } while (new File(filePrefix1 + ".wav").exists());
        ByteArrayInputStream bais = new ByteArrayInputStream(by);
        long length = (long) (by.length / audioFormat.getFrameSize());
        AudioInputStream audioInputStreamTemp = new AudioInputStream(bais, audioFormat, length);
        WaveFileWriter writer = new WaveFileWriter();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filePrefix1 + ".wav");
            writer.write(audioInputStreamTemp, AudioFileFormat.Type.WAVE, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File newFile = new File(filePrefix1 + ".wav");
        final File newFileWub = new File(filePrefix1 + ".wub");
        final AudioObject ao = new AudioObject(by, fa, newFile);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ao.mc.paint1();
                try {
                    Serializer.store(ao, newFileWub);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();

        pause = savePause;
        return ao;
    }

    public double convertByteToTime(int pos) {
        return (double) pos / (double) AudioObject.sampleRate / (double) AudioObject.frameSize;
    }

    public int convertTimeToByte(double time) {
        int c = (int) (time * AudioObject.sampleRate * AudioObject.frameSize);
        c += c % AudioObject.frameSize;
        return c;
    }

    public TimedEvent intersects(TimedEvent i, TimedEvent e, int newbytestart) {
        if (e.start + e.duration <= i.start) {
            return null;
        }
        if (e.start >= i.start + i.duration) {
            return null;
        }
        boolean startInInterval = false;
        boolean endInInterval = false;

        //event e start is in i interval
        if (e.start >= i.start && e.start <= i.start + i.duration) {
            startInInterval = true;
        }

        //event e end is in i interval
        if (e.start + e.duration >= i.start && e.start + e.duration <= i.start + i.duration) {
            endInInterval = true;
        }
        TimedEvent te = null;
        if (startInInterval && endInInterval) {
            te = new TimedEvent(e.start, e.duration, 1f);
        }

        if (startInInterval && !endInInterval) {
            double end = i.start + i.duration;
            te = new TimedEvent(e.start, e.duration - (e.start + e.duration - end), 1f);
        }

        if (!startInInterval && endInInterval) {
            te = new TimedEvent(i.start, e.duration - (i.start - e.start), 1f);
        }

        if (!startInInterval && !endInInterval) {
            te = new TimedEvent(i.start, i.duration, 1f);
        }

        te.start = te.start - i.start + convertByteToTime(newbytestart);
        return te;
    }
}

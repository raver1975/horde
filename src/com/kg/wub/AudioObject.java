package com.kg.wub;

import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.TrackAnalysis;
import com.kg.TheHorde;
import com.kg.python.SpotifyDLTest;
import com.kg.synth.ByteRingBuffer;
import com.kg.synth.Output;
import com.kg.synth.Sequencer;
import com.kg.wub.system.*;
import com.myronmarston.util.MixingAudioInputStream;
import com.sun.media.sound.WaveFileWriter;
import org.json.simple.parser.ParseException;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class AudioObject implements Serializable, Tickable {

    /**
     *
     */
    private static final long serialVersionUID = 379377752113122689L;
    transient int filecount = 0;
    public byte[] data;
    public File file;
    public TrackAnalysis analysis;
    public static String spotifyId;

    public transient int position;
    public transient MusicCanvas mc;
    //    public transient byte[] line;
    public transient Queue<Interval> queue;
    //    public transient Interval currentlyPlaying;
    public transient boolean pause = false;
    public transient boolean loop = false;
    public transient HashMap<String, Interval> midiMap = new HashMap<>();
//	public static double tolerance = .2d;

    //    public static final int resolution = 16;
//    public static final int channels = 2;
//    public static final int frameSize = channels * resolution / 8;
//    public static final int sampleRate = 44100;
//    public static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels, frameSize, sampleRate, false);
//    static final int bufferSize = 8192;
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
            File spotifyFile = new File(System.getProperty("user.dir") + File.separator + "spotify" + String.format("_%.1f", ta.getTempo()).replace('.', '-') + "bpm_" + URLEncoder.encode(spotifyId) + ".mp3");
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
                    bpm = Sequencer.bpm;
                    if (bpm < 1) {
                        bpm = Math.floor(au.analysis.getTempo() * 10f) / 10f;
                        TheHorde.bpm.setTargetValue(bpm);
                    }

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
            bpm = Sequencer.bpm;
            if (bpm < 1) {
                bpm = Math.floor(ta.getTempo() * 10f) / 10f;
                TheHorde.bpm.setTargetValue(bpm);
            }
            System.out.println("newest tempo=" + bpm);


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
        TheHorde.output.addLine(this);
    }


    public int countLength(ArrayList<Interval> list) {
        int duration = 0;
        for (Interval i : list) {
            duration += i.lengthBytes;
        }
        return duration;
    }


    @Override
    public boolean tick(byte[] buffer) {
        //get next buffer length bits
        if (queue.size() == 0) {
            return false;
        }

        if (pause) {
            return false;
        }
        ArrayList<Interval> q = new ArrayList<>(queue);
        if (loop) {
            while (countLength(q) < buffer.length) {
                q.addAll(queue);
            }
            q.addAll(q);
        }
        Iterator<Interval> it = q.iterator();
        Interval exam = it.next();
        if (position < exam.startBytes) {
            position = exam.startBytes;
        }
        for (int hh = 0; hh < buffer.length; ) {
            if (exam != null && position >= exam.endBytes) {
                position = exam.startBytes;
                if (it.hasNext()) {
                    exam = it.next();
                } else {
                    exam = null;
                }
                if (exam != null) {
                    position = exam.startBytes;
                }
                if (loop) {
                    queue.add(queue.peek());
                }
                queue.poll();
            }
            if (exam != null) {
                int canCopy = exam.endBytes - position;
                canCopy = Math.min(buffer.length - hh, canCopy);
                System.arraycopy(data, position, buffer, hh, canCopy);
//                buffer[hh] = data[position];
                hh += canCopy;
                position += canCopy;
            } else {
                while (hh < buffer.length) {
                    buffer[hh++] = 0;
                }
            }

        }
//        if (queue.size() > 0) {
//            Interval iv = queue.peek();
//            while (iv!=null&&!(position >= iv.startBytes && position <= iv.endBytes)) {
//                if (loop) {
//                    queue.add(iv);
//                }
//                queue.poll();
//                iv = queue.peek();
//            }
//        }


//        if (position < queue.peek().startBytes || position >= queue.peek().endBytes) {
//            position = queue.peek().startBytes;
//        }
//        int byteMark = 0;
//        for (Interval i : q) {
//            if (byteMark == buffer.length) {
//                break;
//            }

            /*if (position < i.endBytes - (buffer.length - byteMark)) {
                System.arraycopy(data, position, buffer, byteMark, buffer.length - byteMark);
                position += (buffer.length - byteMark);
                byteMark = buffer.length;
            }// - (buffer.length-byteMark) && position < data.length - (buffer.length-byteMark)) {
            else {
                int bitsLeft = Math.max(0, Math.min(Math.min(i.endBytes - position, buffer.length - byteMark), data.length - position));
                System.arraycopy(data, position, buffer, byteMark, bitsLeft);
                byteMark += bitsLeft;
                position += bitsLeft;
            }
            if (position >= i.endBytes || position >= data.length) {
                Interval ii = queue.poll();
                if (loop) {
                    queue.add(ii);
                }
                byteSkip=1;
            }*/
//        }
//        ByteBuffer bb = ByteBuffer.allocate(buffer.length);
//        for (Interval i : q) {
//            if bb.
//        }

        return true;
    }


//        if (currentlyPlaying == null) {
//            if (!queue.isEmpty()) {
//                currentlyPlaying = queue.poll();
//                jPos = Math.max(0, currentlyPlaying.startBytes);
//            } else {
//                arrayFill(buffer, (byte) 0);
//                return false;
//            }
//        }
////            Interval i = queue.poll();
//        if (breakPlay) {
//            breakPlay = false;
//            currentlyPlaying = null;
////                arrayFill(buffer, (byte) 0);
//            return false;
//        }
//        if (brb == null) {
//            brb = new ByteRingBuffer(Output.BUFFER_SIZE);
//        }
//
//
//        if (jPos <= currentlyPlaying.endBytes - buffer.length && jPos < data.length - buffer.length) {
//            brb.write(data, jPos, buffer.length);
////                System.arraycopy(data, jPos, buffer, 0, Output.BUFFER_SIZE);
//            brb.read(buffer);
//            jPos += buffer.length;
//            position = jPos;
//            return true;
//        }
//
//        if (jPos < currentlyPlaying.endBytes && data.length >= currentlyPlaying.endBytes) {
////                        line.write(data, j, i.endBytes - j);
////                arrayFill(buffer, (byte) 0);
////                System.arraycopy(data, jPos, buffer, 0, currentlyPlaying.endBytes - jPos);
//            brb.write(data, jPos, currentlyPlaying.endBytes - jPos);
////                System.arraycopy(data, jPos, buffer, 0, Output.BUFFER_SIZE);
//            byte[] b = new byte[buffer.length - (currentlyPlaying.endBytes - jPos)];
//            jPos += (currentlyPlaying.endBytes - jPos);
//
//            if (loop) {
//                queue.add(currentlyPlaying);
//            }
//            currentlyPlaying = null;
//            tick(b);
////            jPos += b.length;
//            position = jPos;
//            brb.write(b, 0, b.length);
//            brb.read(buffer);
//            return true;
//        }
//        System.out.println("nevere HERE");
////        System.exit(0);
//        return false;

//        }

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
        mp3InputStream = AudioSystem.getAudioInputStream(new AudioFormat(mp3InputStream.getFormat().getSampleRate(), TheHorde.output.mixingAudioInputStream.getFormat().getSampleSizeInBits(), TheHorde.output.mixingAudioInputStream.getFormat().getChannels(), true, false), mp3InputStream);
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

        mp3InputStream = AudioSystem.getAudioInputStream(TheHorde.output.mixingAudioInputStream.getFormat(), mp3InputStream);

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

    /*public byte[] getLine() {
//        SourceDataLine res = null;
//        DataLine.Info info = new DataLine.Info(SourceDataLine.class, AudioObject.audioFormat);
//        try {
//            res = (SourceDataLine) AudioSystem.getLine(info);
//            res.open(AudioObject.audioFormat);
//            res.start();
//        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//        }
//        for (int i=0;i<4;i++){
//            InputStream temp=null;
//
//             temp = (InputStream) TheHorde.output.buffer5;
//            if (temp.)
//        }
        *//*Control ct[] = res.getControls();
        for (Control c : ct) {
            System.out.println("Control:" + c);
            System.out.println(c.getType().getClass());
        }*//*
//        System.exit(0);

        return TheHorde.output.getLine();
    }*/

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
//        if (currentlyPlaying != null) {
//            ll.add(currentlyPlaying);
//        }
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
        long length = (by.length / TheHorde.output.mixingAudioInputStream.getFormat().getFrameSize());
        AudioInputStream audioInputStreamTemp = new AudioInputStream(bais, TheHorde.output.mixingAudioInputStream.getFormat(), length);
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
        return (double) pos / (double) TheHorde.output.mixingAudioInputStream.getFormat().getSampleRate() / (double) TheHorde.output.mixingAudioInputStream.getFormat().getFrameSize();
    }

    public int convertTimeToByte(double time) {
        int c = (int) (time * TheHorde.output.mixingAudioInputStream.getFormat().getSampleRate() * TheHorde.output.mixingAudioInputStream.getFormat().getFrameSize());
        c += c % TheHorde.output.mixingAudioInputStream.getFormat().getFrameSize();
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

    private static final int SMALL = 16;

    public static void arrayFill(byte[] array, byte value) {
        int len = array.length;
        int lenB = len < SMALL ? len : SMALL;

        for (int i = 0; i < lenB; i++) {
            array[i] = value;
        }

        for (int i = SMALL; i < len; i += i) {
            System.arraycopy(array, 0, array, i, len < i + i ? len - i : i);
        }
    }
}

package com.kg.wub.system;

import com.kg.wub.AudioObject;
import com.sun.media.sound.WaveFileWriter;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PlayingField extends Canvas implements MouseListener, MouseMotionListener, KeyListener, ComponentListener, MouseWheelListener {

    int oldWidth;
    public static JFrame frame;
    private JScrollBar jverticalbar;
    private JScrollBar jhorizontalbar;
    private BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private double offset;
    private Node mover;
    private double movex;
    private double movey;
    private int movex1;
    private int movey1;
    private int currPos;
    private int playPos;
    public transient SourceDataLine line;
    byte[] data;
    private double lengthInPixels;
    private double bytesPerPixel;
    private int lengthInBytes;
    static private int bufferSize = 8192;
    boolean pause = true;
    protected int playByte;
    private boolean moverlock;
    private boolean moverpush;
    private double lastmovery;
    private double lastmoverx;
    int mousePos = 0;
    float hue = 0;

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        Graphics g1 = bufferedImage.getGraphics();
        g1.setColor(Color.black);
        g1.fillRect(0, 0, getWidth(), getHeight());

        hue += .01f;
        final float saturation = 1.0f;// 1.0 for brilliant, 0.0 for dull
        final float luminance = 1f; // 1.0 for brighter, 0.0 for black
        Color nowColor = Color.getHSBColor(hue, saturation, luminance);
        for (Node node : CentralCommand.ccn.nodes) {
            g1.drawImage(node.image, (int) (node.rect.x - offset + .5d), (int) (node.rect.y + .5d), null);
            if (node.isMute()) {
                nowColor = Color.GRAY;
                g1.setColor(new Color(nowColor.getRed(), nowColor.getGreen(), nowColor.getBlue(), 150));
                int w = (int) (node.rect.width + .5d);
                if (w == 0) {
                    w = 1;
                }
                g1.fillRect((int) (node.rect.x - offset + .5d), (int) (node.rect.y + .5d), w, CentralCommand.yOffset);
            } else {
                nowColor = Color.getHSBColor(hue, saturation, luminance);
            }
            g1.setColor(nowColor);
            int w = (int) (node.rect.width + .5d);
            if (w == 0)
                w = 1;
            g1.drawRect((int) (node.rect.x - offset + .5d), (int) (node.rect.y + .5d), w, CentralCommand.yOffset - 1);
            // if (w > 2)
            // g1.drawRect((int) (node.rect.x - offset + .5d) + 1, (int)
            // (node.rect.y + .5d) + 1, w - 2, (int) CentralCommand.yOffset -
            // 3);
            g1.drawString(node.ao.file.getName(), (int) (node.rect.x+5),(int) (node.rect.y + .4d*CentralCommand.yOffset));

        }
        if (mover != null) {
            if (mover.isMute()) {
                nowColor = Color.WHITE;
            } else {
                nowColor = Color.getHSBColor(hue, saturation, luminance);
            }
            g1.setColor(new Color(nowColor.getRed(), nowColor.getGreen(), nowColor.getBlue(), 150));
            int w = (int) (mover.rect.width + .5d);
            if (w == 0)
                w = 1;
            g1.fillRect((int) (mover.rect.x - offset + .5d), (int) (mover.rect.y + .5d), w, CentralCommand.yOffset);
        }
        g1.setColor(Color.red);
        g1.drawLine(currPos, 0, currPos, getHeight());
        g1.setColor(Color.white);
        playPos = (int) (((double) (playByte) / (double) lengthInBytes) * lengthInPixels - offset + .5d);
        g1.drawLine(playPos, 0, playPos, getHeight());
        g.drawImage(bufferedImage, 0, 0, null);
    }

    public PlayingField() {
        oldWidth = 800;
        setSize(new Dimension(oldWidth, 760));
        frame = new JFrame("Play");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // js = new JScrollPane(this);

        frame.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addComponentListener(this);
        this.addKeyListener(this);
        this.addMouseWheelListener(this);
        // js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // frame.getContentPane().add(js, "Center");
        jverticalbar = new JScrollBar(JScrollBar.VERTICAL);
        jverticalbar.setMinimum(oldWidth);
        jverticalbar.setMaximum(2000 * 1000);
        jverticalbar.setValue(oldWidth);
        jverticalbar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent ae) {
                if (ae.getValueIsAdjusting())
                    return;

                double percent = (double) (jhorizontalbar.getValue() + currPos) / oldWidth;
                oldWidth = ae.getValue();

                jverticalbar.revalidate();
                PlayingField.this.revalidate();

                jverticalbar.setUnitIncrement((jverticalbar.getValue() / 5) + 1);
                jverticalbar.setBlockIncrement((jverticalbar.getValue() / 5) + 1);
                jhorizontalbar.setUnitIncrement(getWidth() / 5);
                jhorizontalbar.setBlockIncrement(getWidth() / 5);
                jhorizontalbar.setMinimum(0);
                jhorizontalbar.setMaximum(oldWidth - getWidth());
                jhorizontalbar.setValue((int) (oldWidth * percent) - currPos);
                makeData();

            }
        });

        jhorizontalbar = new JScrollBar(JScrollBar.HORIZONTAL);
        jhorizontalbar.setMinimum(0);
        jhorizontalbar.setMaximum(0);
        jhorizontalbar.setValue(0);
        jhorizontalbar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent ae) {
                offset = ae.getValue();
            }
        });
        frame.getContentPane().add(this, "Center");
        frame.getContentPane().add(jverticalbar, "East");
        frame.getContentPane().add(jhorizontalbar, "South");
        frame.setBounds(100, 100, oldWidth + 50, 760);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - this.getSize().width / 2 + 50, dim.height / 2 - this.getSize().height / 2 + 50);
        frame.setVisible(true);
        frame.validate();
        frame.repaint();
        oldWidth = this.getWidth();
        jhorizontalbar.setUnitIncrement(getWidth() / 5);
        jhorizontalbar.setBlockIncrement(getWidth() / 5);
        // makeImageResize();
        // makeData();

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    repaint();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        startPlaying();
    }

    private void startPlaying() {
        line = getLine();
        new Thread(new Runnable() {

            public void run() {
                top:
                while (true) {
                    if (pause) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        continue top;
                    }
                    playByte += bufferSize;
                    if (playByte + bufferSize >= data.length) {

                        if (playByte < data.length) {
                            int b = data.length - playByte - (data.length - playByte) % AudioObject.frameSize;
                            line.write(data, playByte, b);
                            playByte += bufferSize + b;
                        }
                        pause = true;
                        continue top;
                    }

                    line.write(data, playByte, bufferSize);
                }
            }
        }).start();
    }

    public void makeData() {
        if (CentralCommand.ccn.nodes.size() == 0)
            return;
        double minx = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        for (Node node : CentralCommand.ccn.nodes) {
            if (node.rect.x < minx)
                minx = node.rect.x;
            if (node.rect.width + node.rect.x > maxx)
                maxx = node.rect.width + node.rect.x;

        }
        // minx--;
        // maxx--;
        lengthInPixels = maxx - minx;
        bytesPerPixel = CentralCommand.ccn.nodes.get(0).ao.data.length / CentralCommand.ccn.nodes.get(0).rect.width;
        lengthInBytes = (int) (lengthInPixels * bytesPerPixel);
        lengthInBytes += lengthInBytes % AudioObject.frameSize;

        for (Node node : CentralCommand.ccn.nodes) {
            node.image = new SamplingGraph().createWaveForm(node.ao.analysis.getSegments(), node.ao.analysis.getDuration(), node.ao.data, AudioObject.audioFormat, (int) (node.ao.data.length * (double) oldWidth / lengthInBytes), CentralCommand.yOffset - 1);
            double oldbb = node.rect.width;
            node.rect.width = (node.ao.data.length * (double) oldWidth / lengthInBytes);
            if (node.rect.width < 1)
                node.rect.width = 1;
            node.rect.x /= oldbb / node.rect.width;
        }
        minx = Double.MAX_VALUE;
        maxx = Double.MIN_VALUE;
        for (Node node : CentralCommand.ccn.nodes) {
            if (node.rect.x < minx)
                minx = node.rect.x;
            if (node.rect.width + node.rect.x > maxx)
                maxx = node.rect.width + node.rect.x;
        }
        // minx--;
        // maxx--;
        lengthInPixels = maxx - minx;
        bytesPerPixel = CentralCommand.ccn.nodes.get(0).ao.data.length / CentralCommand.ccn.nodes.get(0).rect.width;
        lengthInBytes = (int) (lengthInPixels * bytesPerPixel);
        lengthInBytes += lengthInBytes % AudioObject.frameSize;
        data = new byte[lengthInBytes];

        for (Node node : CentralCommand.ccn.nodes) {
            if (node.isMute()) {
                continue;
            }
            node.rect.x -= minx;
            int start = (int) (node.rect.x / lengthInPixels * (double) lengthInBytes);
            start -= start % AudioObject.frameSize;
            short g, h;
            for (int i = 0; i < node.ao.data.length; i += 2) {
                g = data[i + start];
                h = data[i + start + 1];
                g += node.ao.data[i];
                if (g > 127) {
                    g = 127;
                    h += 1;
                } else if (g < -128) {
                    g = -128;
                    h -= 1;
                }
                h += node.ao.data[i + 1];
                if (h > 127) {
                    h = 127;
                } else if (h < -128) {
                    h = -128;
                }
                data[i + start] = (byte) g;
                data[i + start + 1] = (byte) h;
            }

        }
        // north.image = new SamplingGraph().createWaveForm(null, data.length /
        // AudioObject.frameSize / AudioObject.channels /
        // AudioObject.resolution, data, AudioObject.audioFormat, getWidth(),
        // CentralCommand.yOffset);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isShiftDown() && Character.isAlphabetic((char) e.getKeyCode())) {
            CentralCommand.key((char) e.getKeyCode() + "");
            System.out.println("keyevent:"+(char) e.getKeyCode() + "");
        }
        if (e.isShiftDown())
            moverlock = true;
        if (e.isControlDown())
            moverpush = true;
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            // if (mover != null)
            // mover.rect.y--;
            jverticalbar.setValue(jverticalbar.getValue() + jverticalbar.getUnitIncrement());
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            jverticalbar.setValue(jverticalbar.getValue() - jverticalbar.getUnitIncrement());
            // if (mover != null)
            // mover.rect.y++;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            jhorizontalbar.setValue(jhorizontalbar.getValue() - jhorizontalbar.getUnitIncrement());
            // if (mover != null)
            // mover.rect.x--;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            jhorizontalbar.setValue(jhorizontalbar.getValue() + jhorizontalbar.getUnitIncrement());
            // if (mover != null)
            // mover.rect.x++;
        } else if (e.getKeyCode() == KeyEvent.VK_M) {
            mover.toggleMute();
            makeData();
        } else if (e.getKeyCode() == KeyEvent.VK_INSERT && mover != null) {
            Node n = new Node(new Rectangle2D.Double(mover.rect.x, mover.rect.y, mover.rect.width, mover.rect.height), mover.ao);
            CentralCommand.addRectangleNoMoveY(n);
            if (CentralCommand.intersects(n)) {
                push(n, mover.rect.width);
            }
            makeData();
        } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            CentralCommand.removeRectangle(mover);

            mover = null;
            makeData();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // playByte = (int) (lengthInBytes * (double)
            // MouseInfo.getPointerInfo().getLocation().x / lengthInPixels);
            // playByte += playByte % AudioObject.frameSize;
            // if (playByte < 0)
            // playByte = 0;
            playByte = mousePos;
            pause = !pause;

        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (e.isShiftDown()) {
                savePlay();
            } else
                saveWave();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            AudioObject.factory();
        }

    }

    private void savePlay() {
        final JFileChooser fc = new JFileChooser(CentralCommand.lastDirectory);
        fc.setFileFilter(new FileNameExtensionFilter("Play File", "play", "Play"));
        int returnVal = fc.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileToBeSaved = fc.getSelectedFile();
            CentralCommand.lastDirectory = fc.getSelectedFile();
            if (!fc.getSelectedFile().getAbsolutePath().endsWith(".play")) {
                fileToBeSaved = new File(fc.getSelectedFile() + ".play");
            }
            try {
                Serializer.store(CentralCommand.ccn, fileToBeSaved);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // do something with the file
        }
    }

    private void saveWave() {
        final JFileChooser fc = new JFileChooser(CentralCommand.lastDirectory);
        fc.setFileFilter(new FileNameExtensionFilter("Wav File", "wav", "WAV"));
        int returnVal = fc.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileToBeSaved = fc.getSelectedFile();
            CentralCommand.lastDirectory = fc.getSelectedFile();
            if (!fc.getSelectedFile().getAbsolutePath().endsWith(".wav")) {

                fileToBeSaved = new File(fc.getSelectedFile() + ".wav");
            }
            save(fileToBeSaved);
            // do something with the file
        }
    }

    private void save(File file) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        long length = (long) (data.length / AudioObject.frameSize);
        AudioInputStream audioInputStreamTemp = new AudioInputStream(bais, AudioObject.audioFormat, length);
        WaveFileWriter writer = new WaveFileWriter();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            writer.write(audioInputStreamTemp, AudioFileFormat.Type.WAVE, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        moverlock = false;
        moverpush = false;

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = (int) (e.getX() + offset);
        int y = e.getY();
        y -= y % CentralCommand.yOffset;
        Point p = new Point(x, y);
        if (mover != null) {
            mover.rect.y = y - movey1 + movey;
            if (moverpush && CentralCommand.intersects(mover)) {
                push(mover, 1);
            }

            if (y < 0 || (moverlock && CentralCommand.intersects(mover))) {
                mover.rect.y = lastmovery;
            }

            mover.rect.x = x - movex1 + movex;
            if (moverpush && CentralCommand.intersects(mover)) {
                push(mover, mover.rect.x - lastmoverx);
            }
            if (moverlock && CentralCommand.intersects(mover)) {
                mover.rect.x = lastmoverx;
            }
            lastmoverx = mover.rect.x;
            lastmovery = mover.rect.y;
        }

        // for (AudioObject au : CentralCommand.aolist) {
        // for (Rectangle r : au.playFieldPosition) {
        // if (r.contains(p)) {
        // mover = r;
        // break;
        // }
        // }
        // }

    }

    public void push(Node n, double d) {
        if (d == 0)
            d = 1;
        push(n, Math.signum(d), new ArrayList<Node>(), 0);
    }

    public void push(Node n, double d, ArrayList<Node> pushed, int cnt) {
        if (cnt > 1000) return;
        ArrayList<Node> copy = new ArrayList<Node>(pushed);
        copy.add(n);
        Node f = null;
        while ((f = CentralCommand.whichIntersects(n, copy)) != null) {
            if (d > 0)
                f.rect.x = n.rect.x + n.rect.width;
            if (d < 0)
                f.rect.x = n.rect.x - f.rect.width;
            push(f, d, copy, cnt++);
        }

        // }

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = (int) (e.getX() + offset);
        int y = e.getY();
        y -= y % CentralCommand.yOffset;
        Point p = new Point(x, y);
        if (!frame.isActive()) {
            frame.requestFocus();
            frame.toFront();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        for (Node node : CentralCommand.ccn.nodes) {
            if (node.rect.contains(p)) {
                mover = node;
                movex = mover.rect.x;
                movey = mover.rect.y;
                movex1 = x;
                movey1 = y;
                lastmoverx = mover.rect.x;
                lastmovery = mover.rect.y;
                break;
            }
        }

        currPos = e.getX();

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = (int) (e.getX() + offset);
        int y = e.getY();
        y -= y % CentralCommand.yOffset;
        Point p = new Point(x, y);
        if (e.getButton() == MouseEvent.BUTTON3) {
            pause = false;
            // playPos = (int) (((double) playByte / (double) lengthInBytes) *
            // getWidth());
            // lengthinbytes*playpos/width=playbyte
            playByte = (int) (lengthInBytes * (double) (x) / lengthInPixels);
            playByte += playByte % AudioObject.frameSize;
            if (playByte < 0)
                playByte = 0;
            pause = false;
            mousePos = playByte;
        }
        mover = null;
        for (Node node : CentralCommand.ccn.nodes) {
            if (node.rect.contains(p)) {
                mover = node;
                movex = mover.rect.x;
                movey = mover.rect.y;
                movex1 = x;
                movey1 = y;
                lastmoverx = mover.rect.x;
                lastmovery = mover.rect.y;
                if (e.getClickCount() == 2) {
                    mover.ao.mc.frame.setVisible(true);
                }
                break;
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        makeData();

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void componentResized(ComponentEvent e) {
        bufferedImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        oldWidth = getWidth();
        jverticalbar.setMinimum(oldWidth);
        if (jverticalbar.getValue() < oldWidth)
            jverticalbar.setValue(oldWidth);
        makeData();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void componentShown(ComponentEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // TODO Auto-generated method stub

        jverticalbar.setValue(jverticalbar.getValue() + e.getWheelRotation() * ((-jverticalbar.getValue() / 10) + -jverticalbar.getValue() / jverticalbar.getValue()));
        // jhorizontalbar
        // .setValue((int) (jhorizontalbar.getValue()+
        // (getWidth()-getWidth()*2*oldWidthp / (double) oldWidth)));

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

    public int convertTimeToByte(double time) {
        int c = (int) (time * AudioObject.sampleRate * AudioObject.frameSize);
        c += c % AudioObject.frameSize;
        return c;
    }

    // public int convertByteToPixel(int loc){
    // (double)loc/double()getWidth
    // }
}

package io.github.coderodde.human.tool.hearing;

import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * This class implements the beeping thread.
 */
public final class BeepingThread extends Thread {
    
    private static final float SAMPLE_RATE = 44_100.0f;
    public  static final int MIN_FREQUENCY = 10;
    private static final int MAX_FREQUENCY = 20_000;
    private static final int BYTES_PER_SAMPLE = 2;
    private static final double TWO_PI = 2.0 * Math.PI;
    
    private volatile boolean running = true;
    private volatile boolean paused  = true;
    
    private final Object lock = new Object();
    private final Label label;
    
    private double phase = 0.0;
    private int frequency = MIN_FREQUENCY;
    
    public BeepingThread(Label label) {
        this.label = label;
        setDaemon(true);
    }
    
    @Override
    public void run() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE,
                                             16,
                                             1,
                                             true,
                                             false);
        
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            
            while (running && frequency <= MAX_FREQUENCY) {
                waitIfPaused();
                
                if (!running) {
                    break;
                }
                
                playOneMillisecond(line);
                
                int currentFrequency = frequency;
                
                Platform.runLater(
                    () -> label.setText(currentFrequency + " Hz"));
                
                ++frequency;
            }
            
            line.drain();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(getClass()
                  .getSimpleName()).log(SEVERE, "Audio failed.");
            
            return;
        }
    }

    private void playOneMillisecond(SourceDataLine line) {
        int samples = samplesInOneMillisecond(frequency);
        byte[] buffer = new byte[samples * BYTES_PER_SAMPLE];
        
        double phaseIncrement = TWO_PI * frequency / SAMPLE_RATE;
        
        int index = 0;
        
        for (int i = 0; i < samples; ++i) {
            short sample = (short) (Math.sin(phase) * Short.MAX_VALUE);
            
            buffer[index++] = (byte) (sample & 0xff);
            buffer[index++] = (byte) (sample >>> 8);
            
            phase += phaseIncrement;
            
            if (phase >= TWO_PI) {
                phase -= TWO_PI;
            }
        }
        
        line.write(buffer, 0, buffer.length);
    }
    
    private static int samplesInOneMillisecond(int millisecondIndex) {
        int a = (int) Math.round(SAMPLE_RATE *  millisecondIndex / 1000.0);
        int b = (int) Math.round(SAMPLE_RATE * (millisecondIndex + 1) / 1000.0);
        
        return b - a;
    }
    
    private void waitIfPaused() {
        synchronized (lock) {
            while (paused && running) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    running = false;
                    return;
                }
            }
        }
    }
        
    public void pauseBeeping() {
        paused = true;
    }
    
    public void resumeBeeping() {
        synchronized (lock) {
            paused = false;
            lock.notifyAll();
        }
    }
    
    public void terminate() {
        running = false;
        
        synchronized (lock) {
            lock.notifyAll();
        }
        
        interrupt();
    }
}

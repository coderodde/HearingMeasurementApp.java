package io.github.coderodde.human.tool.hearing;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javax.sound.sampled.LineUnavailableException;

/**
 * This class implements the beeping thread.
 */
public final class BeepingThread extends Thread {
    
    private volatile boolean running = true;
    private volatile boolean paused  = true;
    
    private final Object lock = new Object();
    private final Label label;
    
    public BeepingThread(Label label) {
        this.label = label;
    }
    
    @Override
    public void run() {
        int frequency = HearingMeasurementApp.MINIMUM_FREQUENCY;
        
        while (running) {
            synchronized (lock) {
                while (paused && running) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
            
            int currentFrequency = frequency;
            
            Platform.runLater(() -> {
                label.setText(String.format("%d Hz", currentFrequency));
            });
            
            try {
                Beeper.beep(frequency, 20);
            } catch (LineUnavailableException ex) {
                System.getLogger(BeepingThread.class.getName())
                    .log(System.Logger.Level.ERROR, (String) null, ex);
                
                return;
            }
            
            frequency += 1;
//            
//            try {
//                Thread.sleep(20L);
//            } catch (InterruptedException ignored) {
//                
//            }
            
            if (frequency > HearingMeasurementApp.MAXIMUM_FREQUENCY) {
                return;
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

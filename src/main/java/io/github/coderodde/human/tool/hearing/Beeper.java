package io.github.coderodde.human.tool.hearing;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * This class provides the method for beeping.
 */
public final class Beeper {
    
    private static final float SAMPLE_RATE = 44_100.0f;
    
    private Beeper() {
        
    }
    
    /**
     * Attempts to play a sine wave with incrementing frequency. One integer 
     * frequency per millisecond.
     * 
     * @param minimumFrequency the minimum frequency.
     * @param maximumFrequency the maximum frequency.
     * @throws javax.sound.sampled.LineUnavailableException if audio machinery
     *                                                      fails.
     */
    public static void beep(int minimumFrequency, int maximumFrequency) 
    throws LineUnavailableException {
        
        AudioFormat format = new AudioFormat(SAMPLE_RATE,
                                             16,
                                             1,
                                             true,
                                             false);
        
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();
        
        final int milliseconds = maximumFrequency
                               - minimumFrequency 
                               + 1;
        
        final int samplesPerMillisecond = 
            (int) Math.round(SAMPLE_RATE / 1000.0);
        
        byte[] buffer = new byte[2 * milliseconds * samplesPerMillisecond];
        
        double phase = 0.0;
        
        int index = 0;
        
        for (int frequency = minimumFrequency;
                 frequency <= maximumFrequency;
                 frequency++) {
            
            double phaseIncrement = 2.0 * Math.PI * frequency / SAMPLE_RATE;
            
            for (int i = 0; i < samplesPerMillisecond; ++i) {
                short sample = (short) (Math.sin(phase) * Short.MAX_VALUE);

                buffer[index++] = (byte) (sample);
                buffer[index++] = (byte) (sample >>> 8);

                phase += phaseIncrement;
                
                if (phase >= 2.0 * Math.PI) {
                    phase -= 2.0 * Math.PI;
                }
            }
        }
        
        line.write(buffer, 0, buffer.length);
        line.drain();
        line.close();
    }
}

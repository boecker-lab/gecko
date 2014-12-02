package de.unijena.bioinf.gecko3.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Created by swinter on 05.11.2014.
 */
public class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultUncaughtExceptionHandler.class);

    public void uncaughtException(Thread t, Throwable e) {
        // Here you should have a more robust, permanent record of problems
        JOptionPane.showMessageDialog(findActiveFrame(),
                e.getMessage(), "Exception Occurred", JOptionPane.ERROR_MESSAGE);
        logger.error("CatchALL", e);
    }
    private Frame findActiveFrame() {
        Frame[] frames = JFrame.getFrames();
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].isVisible()) {
                return frames[i];
            }
        }
        return null;
    }

}

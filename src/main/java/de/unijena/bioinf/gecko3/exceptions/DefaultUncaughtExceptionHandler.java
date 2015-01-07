/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

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

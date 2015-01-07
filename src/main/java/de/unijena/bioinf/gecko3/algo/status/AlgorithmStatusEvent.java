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

package de.unijena.bioinf.gecko3.algo.status;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class AlgorithmStatusEvent {
    public enum Task {
        Init, ComputingClusters, ComputingStatistics, Done
    }

    private final int progress;
    private final Task task;

    public AlgorithmStatusEvent(int progress, Task task) {
        this.progress = progress;
        this.task = task;
    }

    public int getProgress() {
        return progress;
    }

    public Task getTask() {
        return task;
    }
}

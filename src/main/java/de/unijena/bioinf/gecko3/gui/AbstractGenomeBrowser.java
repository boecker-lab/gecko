/*
 * Copyright 2014 Sascha Winter
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

package de.unijena.bioinf.gecko3.gui;

import de.unijena.bioinf.gecko3.datastructures.Genome;

import javax.swing.*;
import java.awt.*;

abstract class AbstractGenomeBrowser extends JScrollPane implements Adjustable{
	public abstract void adjustScrollPosition(int value);
	public abstract void scrollToPosition(int chromosomeIndex, int geneIndex);
	public abstract boolean isFlipped();
	public abstract void flip();


    /**
     * The whole genome browser is understated, no cluster contained
     */
    public abstract void highlightCluster();

    /**
     * Highlights the given interval in the visualisation, the rest is understated
     * @param chr
     * @param start
     * @param stop
     * @param highlightColor
     */
	public abstract void highlightCluster(int chr, int start, int stop, Color highlightColor);
	public abstract void clearHighlight();
	public abstract Genome getGenome();
	public abstract int getGBHeight();
    public abstract void setNameType(GenomePainting.NameType nameType);
}

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

package de.unijena.bioinf.gecko3.io;

import de.unijena.bioinf.gecko3.exceptions.LinePassedException;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

public class CountedReader implements AutoCloseable {
	
	private final BufferedReader buff;
	private int counter;
	private boolean eof;
	
	public CountedReader(Reader r) {
		this.buff = new BufferedReader(r);
		this.counter = -1;
		this.eof = false;
	}
	
	public String readLine() throws IOException {
		String s = this.buff.readLine();
		if (s!=null) this.counter++;
		else this.eof = true;
		return s;
	}

	/**
	 * Skips a given number of lines
	 * @param l The line to skip to. The next readLine() call will return the contents of line l if it exists.
	 * @throws IOException, EOFException If the number given -1 exceeds the length of the content.
	 * @throws LinePassedException If the readers pointer already points at a line after the given line number
	 */
	public void jumpToLine(int l) throws IOException, LinePassedException {
		if (this.eof) throw new EOFException();
		if (this.counter > l-1) throw new LinePassedException();
		while (this.counter < l-1) {
			String line = this.buff.readLine();
			if (line==null) {
				this.eof = true;
				throw new EOFException();
			}
			this.counter++;
		}
	}
	
	/**
	 * Returns the number of the line that will be returned at the next readLine() call.
	 * @return the number of the line that will be returned at the next readLine() call.
	 */
	public int getCurrentLineNumber() {
		return counter;
	}

    public void close() throws IOException {
        if (buff != null)
            buff.close();
    }
}

package gecko2.io;

import gecko2.exceptions.LinePassedException;

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

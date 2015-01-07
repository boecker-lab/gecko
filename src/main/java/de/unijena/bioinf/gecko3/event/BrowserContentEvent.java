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

package de.unijena.bioinf.gecko3.event;

import de.unijena.bioinf.gecko3.gui.MultipleGenomesBrowser;

import java.util.EventObject;

public class BrowserContentEvent extends EventObject {
	
	public static final short SCROLL_VALUE_CHANGED = 1;
	public static final short ZOOM_FACTOR_CHANGED = 2;
	
	private final short eventType;
	private static final long serialVersionUID = 1382632021469547584L;

	public BrowserContentEvent(MultipleGenomesBrowser source, short eventType) {
		super(source);
		this.eventType = eventType;
	}
	
	public short getEventType() {
		return eventType;
	}
	
	@Override
	public MultipleGenomesBrowser getSource() {
		return (MultipleGenomesBrowser) super.getSource();
	}
}


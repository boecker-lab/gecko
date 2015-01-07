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

package de.unijena.bioinf.gecko3.event;

import de.unijena.bioinf.gecko3.GeckoInstance;

import java.util.EventObject;

public class DataEvent extends EventObject {

	private static final long serialVersionUID = 2868748485667954767L;

	public DataEvent(GeckoInstance source) {
		super(source);
	}
	
	@Override
	public GeckoInstance getSource() {
		return (GeckoInstance) super.getSource();
	}
	
}

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

import de.unijena.bioinf.gecko3.datastructures.GeneCluster;

import java.util.EventObject;

public class ClusterSelectionEvent extends EventObject {

	/**
	 * Random generated serialization UID
	 */
	private static final long serialVersionUID = -9184708680298812763L;
	private final GeneCluster selection;
	private final boolean instantDisplay;
	
	public ClusterSelectionEvent(Object source, GeneCluster selection, boolean instantDisplay) {
		super(source);
		this.selection = selection;
		this.instantDisplay = instantDisplay;
	}
	
	public ClusterSelectionEvent(Object source, GeneCluster selection) {
		this(source,selection,false);
	}

	public boolean isInstantDisplayEnabled() {
		return instantDisplay;
	}
	
	public GeneCluster getSelection() {
		return selection;
	}

}

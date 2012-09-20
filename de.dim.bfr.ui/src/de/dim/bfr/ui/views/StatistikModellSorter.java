/*******************************************************************************
 * Copyright (C) 2012 Data In Motion
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * Project: de.dim.bfr.ui
 * $HeadURL: http://data-in-motion.biz/statup/trunk/de.dim.bfr.ui/src/de/dim/bfr/ui/views/StatistikModellSorter.java $
 * $LastChangedDate: 2012-01-24 11:03:21 +0100 (Di, 24 Jan 2012) $
 * $lastChangedBy$
 * $Revision: 652 $
 * (c) Data in Motion 2011
 */
package de.dim.bfr.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import de.dim.bfr.StatistikModell;

/**
 * Sorter for the statistic model view. It sorts first by id and after that by name
 * @author Mark Hoffmann
 * @since 14.11.2011
 */
public class StatistikModellSorter extends ViewerSorter {
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		StatistikModell m1 = (StatistikModell) e1;
		StatistikModell m2 = (StatistikModell) e2;
		if (m1.getId() < m2.getId()) {
			return -1;
		} else if (m1.getId() > m2.getId()) {
			return 1;
		} else {
			return m1.getName().compareToIgnoreCase(m2.getName());
		}
	}

}

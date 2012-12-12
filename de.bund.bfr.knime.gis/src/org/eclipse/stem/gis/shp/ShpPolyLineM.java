/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.stem.gis.shp;

import org.eclipse.stem.gis.shp.type.Box;
import org.eclipse.stem.gis.shp.type.Part;
import org.eclipse.stem.gis.shp.type.Range;

public class ShpPolyLineM extends ShpPolyLine
{
	protected Range mRange;

	ShpPolyLineM()
	{
		this(null,null,null);
	}
	
	public ShpPolyLineM(Box boundingBox, Part[] parts, Range mRange) 
	{
		super(boundingBox, parts);
		this.mRange = mRange;
	}
	
	void setMRange(Range mRange) {
		this.mRange = mRange;
	}

	public Range getMRange() {
		return mRange;
	}
	
	public int getType()
	{
		return ShpConstants.SHP_POLY_LINE_M;
	}
	
	
}

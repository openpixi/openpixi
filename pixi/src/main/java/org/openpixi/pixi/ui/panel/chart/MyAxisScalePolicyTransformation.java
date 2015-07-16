/*
 * MODIFICATION:
 *
 * Allow for logarithmic values < 1.
 */

/*
 *  AxisScalePolicyAutomaticBestFit.java of project jchart2d, <enterpurposehere>.
 *  Copyright (C) 2002 - 2013, Achim Westermann, created on Apr 22, 2011
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 *
 * File   : $Source: /cvsroot/jchart2d/jchart2d/codetemplates.xml,v $
 * Date   : $Date: 2009/02/24 16:45:41 $
 * Version: $Revision: 1.2 $
 */

package org.openpixi.pixi.ui.panel.chart;

import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.LabeledValue;
import info.monitorenter.gui.chart.axis.AAxisTransformation;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyTransformation;
import info.monitorenter.util.Range;

import java.awt.Graphics;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Very basic implementation that has to be used with implementation of
 * {@link AAxisTransformation} to have the scale transformed.
 * <p>
 *
 * @author Bill Schoolfield (contributor)
 *
 * @author Achim Westermann (modification)
 *
 */

public class MyAxisScalePolicyTransformation extends
		AxisScalePolicyTransformation {

	/**
	 * Uses the transformation function callbacks (
	 * {@link AAxisTransformation#transform(double)},
	 * {@link AAxisTransformation#untransform(double)}) of the
	 * {@link AAxisTransformation} this instance may be used with to have the
	 * scale transformed accordingly.
	 * <p>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<LabeledValue> getScaleValues(final Graphics g2d,
			final IAxis<?> axis) {

		// Might give a class cast exception in case this was not called from
		// the AAxisTranfsormation itself:
		AAxisTransformation<AxisScalePolicyTransformation> axisTransformation = (AAxisTransformation<AxisScalePolicyTransformation>) axis;
		final List<LabeledValue> collect = new LinkedList<LabeledValue>();
		LabeledValue label;

		final Range domain = axis.getRange();

		double min = domain.getMin();
		double max = domain.getMax();

		min = axisTransformation.transform(min);
		max = axisTransformation.transform(max);

		double range = max - min;

		double exp = Math.floor(min);
		double val = axisTransformation.untransform(exp);

		double axisMax = axis.getMax();
		double axisMin = axis.getMin();
		while (val <= axisMax) {
			if (val >= axisMin) {
				label = new LabeledValue();
				label.setValue(val);
				// label.setLabel(axis.getFormatter().format(label.getValue()));
				label.setLabel(new DecimalFormat("0.0E0").format(label
						.getValue()));
				label.setMajorTick(true);

				label.setValue((axisTransformation.transform(label.getValue()) - min)
						/ range);
				collect.add(label);
			}

			exp += 1.;
			val = axisTransformation.untransform(exp);
		}

		return collect;
	}

	@Override
	public void initPaintIteration(IAxis<?> axis) {
		// nop
	}

}

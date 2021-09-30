/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.GUI;

import java.awt.*;

/**
 *  <pre>
 *  Copyright (c) 1997-2006, Gregg Wonderly
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *  * The name of the author may not be used to endorse or promote
 *    products derived from this software without specific prior
 *    written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *  OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *
 *  This interface is returned/used by the {@link org.wonderly.awt.Packer} layout manager.  The
 *  methods are used to arrange the layout of components.
 *
 *  The majority of these methods correspond directly to elements of the
 *  {@link java.awt.GridBagConstraints} object and its use with the
 *  {@link java.awt.GridBagLayout} layout
 *  manager.  The purpose of this class is to make the use of these two
 *  objects simpler and less error prone by allowing the complete layout
 *  of an object to be specified in a single line of code, and to discourage
 *  the reuse of GridBagConstraint Objects which leads to subtle layout
 *  interactions.
 *  <p>
 *  <b>NOTE:</b>:<br>
 *  Pay attention to the fact that {@link #fillx()}, {@link #filly()} and {@link #fillboth()} change
 *  the weights of the respective axis.  This is done to unify the expansion
 *  factors to equally distribute the components if you do not specify <code>weightx/weighty</code>
 *  values.  If the <code>weightx/weighty</code> values are not specified explicitly, they default to 1.
 *  Either put the {@link #weightx(double)}/{@link #weighty(double)} call after the
 *  {@link #fillx()}/{@link #filly()} call to get
 *  something different than equal expansion, or use the {@link #fillx(double)},
 *  {@link #filly(double)} or {@link #fillboth(double,double)} methods that combine
 *  these two calls.
 *  <p>
 *  When stacking several buttons, you can use
 *  <code>...fillx(0)</code> to make all of them the same width,
 *  but keep the container/column from being any wider than the widest button.
 *
 *  @version 3.0
 *  @author <a href="mailto:gregg@wonderly.org">Gregg Wonderly</a>
 *  @see org.wonderly.awt.Packer
 */

public interface PackAs extends java.io.Serializable {
	/**
	 *	Set the passed container as the container to pack
	 *	future components into.
	 *
	 *  @exception IllegalAccessException when the container is already set and
	 *             cloning the Packer instance fails.
	 */
	public PackAs into( Container cont ) throws IllegalAccessException;
	/**
	 *	Specifies the insets to apply to the component.
	 *	@param insets the insets to apply
	 */
	public PackAs inset( Insets insets );
	/**
	 *	Specifies the insets to apply to the component.
	 *	@param left the left side inset.
	 *	@param top the top inset.
	 *	@param right the right side inset.
	 *	@param bottom the bottom inset.
	 */
	public PackAs inset( int top, int left, int bottom, int right );
	/**
	 *	Add anchor=NORTH to the constraints for the current
	 *	component.
	 */
	public PackAs north();
	/**
	 *	Add anchor=SOUTH to the constraints for the current
	 *	component.
	 */
	public PackAs south();
	/**
	 *	Add anchor=EAST to the constraints for the current
	 *	component.
	 */
	public PackAs east();
	/**
	 *	Add anchor=WEST to the constraints for the current
	 *	component.
	 */
	public PackAs west();
	/**
	 *	Add anchor=NORTHWEST to the constraints for the current
	 *	component.
	 */
	public PackAs northwest();
	/**
	 *	Add anchor=SOUTHWEST to the constraints for the current
	 *	component.
	 */
	public PackAs southwest();
	/**
	 *	Add anchor=NORTHEAST to the constraints for the current
	 *	component.
	 */
	public PackAs northeast();
	/**
	 *	Add anchor=SOUTHEAST to the constraints for the current
	 *	component.
	 */
	public PackAs southeast();
	/**
	 *  Add gridx=RELATIVE to the constraints for the current
	 *	component.
	 */
	public PackAs left();
	/**
	 *  Add gridy=RELATIVE to the constraints for the current
	 *	component.
	 */
	public PackAs top();
	/**
	 *  Add gridx=REMAINDER to the constraints for the current
	 *	component.
	 */
	public PackAs right();
	/**
	 *  Add gridy=REMAINDER to the constraints for the current
	 *	component.
	 */
	public PackAs bottom();
	/**
	 *  Add gridx=tot to the constraints for the current
	 *	component.
	 *
	 *	@param pos - the value to set gridx to.
	 */
	public PackAs gridx( int pos );
	/**
	 *  Add gridy=tot to the constraints for the current
	 *	component.
	 *
	 *	@param pos - the value to set gridy to.
	 */
	public PackAs gridy( int pos );
	/**
	 *  Add gridheight=tot to the constraints for the current
	 *	component.
	 *
	 *	@param cnt - the value to set gridheight to.
	 */
	public PackAs gridh( int cnt );
	/**
	 *  Add gridwidth=tot to the constraints for the current
	 *	component.
	 *
	 *	@param cnt - the value to set gridwidth to.
	 */
	public PackAs gridw( int cnt );
	/**
	 *  Add ipadx=cnt to the constraints for the current
	 *	component.
	 *
	 *	@param cnt - the value to set ipadx to.
	 */
	public PackAs padx( int cnt );
	/**
	 *  Add ipady=cnt to the constraints for the current
	 *	component.
	 *
	 *	@param cnt - the value to set ipady to.
	 */
	public PackAs pady( int cnt );
	/**
	 *  Add fill=HORIZONTAL,weightx=wtx to the constraints for the current
	 *	component.
	 */
	public PackAs fillx(double wtx);
	/**
	 *  Add fill=HORIZONTAL,weightx=1 to the constraints for the current
	 *	component.
	 */
	public PackAs fillx();
	/**
	 *  Add fill=VERTICAL,weighty=wty to the constraints for the current
	 *	component.
	 */
	public PackAs filly(double wty);
	/**
	 *  Add fill=VERTICAL,weighty=1 to the constraints for the current
	 *	component.
	 */
	public PackAs filly();
	/**
	 *  Add fill=BOTH,weighty=1,weightx=1 to the constraints for the current
	 *	component.
	 */
	public PackAs fillboth();
	/**
	 *  Add fill=BOTH,weighty=wty,weightx=wtx to the constraints for the current
	 *	component.
	 */
	public PackAs fillboth(double wtx, double wty);
	/**
	 *  Add weightx=wt to the constraints for the current
	 *	component.
	 *
	 *	@param wt - the value to set weightx to.
	 */
	public PackAs weightx( double wt );
	/**
	 *  Add weighty=wt to the constraints for the current
	 *	component.
	 *
	 *	@param wt - the value to set weightx to.
	 */
	public PackAs weighty( double wt );
	/**
	 *  Reuses the previous set of constraints to layout the passed Component.
	 *
	 *  @param c The component to layout.
	 */
	public PackAs add( Component c );

	/**
	 *  Creates a new set of constraints to layout the passed Component.
	 *
	 *  @param c The component to layout.
	 */
	public PackAs pack( Component c );

	/**
	 *  Add gridwidth=REMAINDER to the constraints for the current
	 *	component.
	 */
	public PackAs remainx();

	/**
	 *  Add gridheight=REMAINDER to the constraints for the current
	 *	component.
	 */
	public PackAs remainy();

	/**
	 *  Reset constraints to those previously used for the passed
	 *  Component.
	 */
	public PackAs like(Component c);

	/**
	 *  Set the anchor term to the passed mask.
	 *  The mask values are described in the {@link java.awt.GridbagConstraints}
	 *  javadoc.  This provides a simple way to support the
	 *  JDK1.6 constraints without having Packer/PackAs
	 *  branch.  In the future, we can add appropriate
	 *  methods for the baseline etc constraints.
	 */
	public PackAs anchor( int mask );
}

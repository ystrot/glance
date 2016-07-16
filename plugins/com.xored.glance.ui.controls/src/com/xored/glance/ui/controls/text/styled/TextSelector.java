/*******************************************************************************
 * Copyright (c) 2012 xored software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and implementation (Yuri Strot)
 ******************************************************************************/
package com.xored.glance.ui.controls.text.styled;

import org.eclipse.jface.text.Region;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;

import com.xored.glance.ui.sources.Match;

/**
 * For all rich text components we remove native selection (make it empty) to
 * draw our own colorful current match. However if focus was moved back to text
 * component, selection should be drawn as usually.
 *
 * TextSelector manages this behavior.
 *
 * @author Yuri Strot
 */
public abstract class TextSelector implements FocusListener {

	public void setMatch(Match match) {
		this.match = match;
		if (match != null) {
			reveal(match.getOffset(), match.getLength());
		}
	}

	public void dispose() {
		Control control = getControl();
		if (!control.isDisposed()) {
			control.removeFocusListener(this);
		}
		showSelection();
	}

	public void focusLost(FocusEvent e) {
		hideSelection();
	}

	public void focusGained(FocusEvent e) {
		showSelection();
	}

	protected void init() {
		getControl().addFocusListener(this);
		hideSelection();
	}

	private void hideSelection() {
		Region region = getSelection();
		setSelection(region.getOffset() + region.getLength(), 0);
	}

	private void showSelection() {
		if (match != null) {
			setSelection(match.getOffset(), match.getLength());
		}
	}

	protected abstract Control getControl();

	protected abstract Region getSelection();

	protected abstract void setSelection(int offset, int length);

	protected abstract void reveal(int offset, int length);

	private Match match;

}

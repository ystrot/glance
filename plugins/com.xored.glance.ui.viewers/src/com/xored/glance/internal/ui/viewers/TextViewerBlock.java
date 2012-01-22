/******************************************************************************* 
 * Copyright (c) 2008 xored software, Inc.  
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     xored software, Inc. - initial API and Implementation (Yuri Strot) 
 *******************************************************************************/
package com.xored.glance.internal.ui.viewers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;

import com.xored.glance.ui.sources.ITextBlock;
import com.xored.glance.ui.sources.ITextBlockListener;
import com.xored.glance.ui.sources.TextChangedEvent;

/**
 * @author Yuri Strot
 * 
 */
public class TextViewerBlock implements ITextBlock, ITextListener {

	public TextViewerBlock(TextViewer viewer) {
		this.viewer = viewer;
		listeners = new ListenerList();
		addListeners();
	}

	public void dispose() {
		removeListeners();
	}

	protected void addListeners() {
		viewer.addTextListener(this);
	}

	protected void removeListeners() {
		viewer.removeTextListener(this);
	}

	public void textChanged(TextEvent event) {
		if (event.getDocumentEvent() != null) {
			TextChangedEvent changedEvent = new TextChangedEvent(event
					.getOffset(), event.getLength(), event.getReplacedText());
			fireTextChanged(changedEvent);
		}
	}

	protected void fireTextChanged(TextChangedEvent changedEvent) {
		Object[] objects = listeners.getListeners();
		for (Object object : objects) {
			ITextBlockListener listener = (ITextBlockListener) object;
			listener.textChanged(changedEvent);
		}
	}

	public void addTextBlockListener(ITextBlockListener listener) {
		listeners.add(listener);
	}

	public String getText() {
		return viewer.getDocument().get();
	}

	public void removeTextBlockListener(ITextBlockListener listener) {
		listeners.remove(listener);
	}

	public int compareTo(ITextBlock o) {
		return 0;
	}

	private ListenerList listeners;
	protected TextViewer viewer;

}

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
package com.xored.glance.internal.ui.viewers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Point;

import com.xored.glance.ui.sources.BaseTextSource;
import com.xored.glance.ui.sources.ITextBlock;
import com.xored.glance.ui.sources.ITextSourceListener;
import com.xored.glance.ui.sources.Match;
import com.xored.glance.ui.sources.SourceSelection;

/**
 * @author Yuri Strot
 * 
 */
public class TextViewerControl extends BaseTextSource implements
		ISelectionChangedListener {

	public TextViewerControl(TextViewer viewer) {
		this.viewer = viewer;
		listeners = new ListenerList();
		blocks = new ColoredTextViewerBlock[] { new ColoredTextViewerBlock(
				viewer) };
		viewer.addSelectionChangedListener(this);
	}

	public void addTextSourceListener(ITextSourceListener listener) {
		listeners.add(listener);
	}

	public void removeTextSourceListener(ITextSourceListener listener) {
		listeners.remove(listener);
	}

	public void dispose() {
		if (!disposed) {
		    if (getBlock().getSelected() != null){
		        selectText(getBlock().getSelected());
		    }
		    
			viewer.removeSelectionChangedListener(this);
			getBlock().dispose();
			disposed = true;
		}
	}

	private void selectText(Match match){
	    TextSelection selection = new TextSelection(match.getOffset(), match.getLength());
        viewer.setSelection(selection, true);
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof TextSelection) {
			TextSelection tSelection = (TextSelection) selection;
			SourceSelection sSelection = new SourceSelection(getBlock(),
					tSelection.getOffset(), tSelection.getLength());
			Object[] objects = listeners.getListeners();
			for (Object object : objects) {
				ITextSourceListener listener = (ITextSourceListener) object;
				listener.selectionChanged(sSelection);
			}
		}
	}

	public boolean isDisposed() {
		return disposed;
	}

	public ColoredTextViewerBlock getBlock() {
		return blocks[0];
	}

	public ITextBlock[] getBlocks() {
		return blocks;
	}

	public SourceSelection getSelection() {
		Point selection = viewer.getSelectedRange();
		return new SourceSelection(getBlock(), selection.x, selection.y);
	}

	public void select(Match match) {

	    getBlock().setSelected(match);
	    
		if (match != null){
			viewer.revealRange(match.getOffset(), match.getLength());
		}
	}

	public void show(Match[] matches) {
		getBlock().setMatches(matches);
	}

	private final ListenerList listeners;
	private boolean disposed;
	private final ColoredTextViewerBlock[] blocks;
	private final TextViewer viewer;

}

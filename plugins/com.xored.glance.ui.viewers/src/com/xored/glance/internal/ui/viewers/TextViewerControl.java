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

    public TextViewerControl(final TextViewer viewer) {
		this.viewer = viewer;
		listeners = new ListenerList();
		blocks = new ColoredTextViewerBlock[] { new ColoredTextViewerBlock(
				viewer) };
		viewer.addSelectionChangedListener(this);
	}

    @Override
    public void addTextSourceListener(final ITextSourceListener listener) {
		listeners.add(listener);
	}

    @Override
    public void removeTextSourceListener(final ITextSourceListener listener) {
		listeners.remove(listener);
	}

    @Override
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

    private void selectText(final Match match) {
        final TextSelection selection = new TextSelection(match.getOffset(), match.getLength());
        viewer.setSelection(selection, true);
	}
	
    @Override
    public void selectionChanged(final SelectionChangedEvent event) {
        final ISelection selection = event.getSelection();
		if (selection instanceof TextSelection) {
            final TextSelection tSelection = (TextSelection) selection;
            final SourceSelection sSelection = new SourceSelection(getBlock(),
					tSelection.getOffset(), tSelection.getLength());
            final Object[] objects = listeners.getListeners();
            for (final Object object : objects) {
                final ITextSourceListener listener = (ITextSourceListener) object;
				listener.selectionChanged(sSelection);
			}
		}
	}

    @Override
    public boolean isDisposed() {
		return disposed;
	}

	public ColoredTextViewerBlock getBlock() {
		return blocks[0];
	}

    @Override
    public ITextBlock[] getBlocks() {
		return blocks;
	}

    @Override
    public SourceSelection getSelection() {
        final Point selection = viewer.getSelectedRange();
		return new SourceSelection(getBlock(), selection.x, selection.y);
	}

    @Override
    public void select(final Match match) {
	    getBlock().setSelected(match);
	    
		if (match != null){
			viewer.revealRange(match.getOffset(), match.getLength());
		}
	}

    @Override
    public void show(final Match[] matches) {
        getBlock().setMatches(matches);
	}

	private final ListenerList listeners;
	private boolean disposed;
	private final ColoredTextViewerBlock[] blocks;
	private final TextViewer viewer;

}

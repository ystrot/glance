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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Point;

import com.xored.glance.ui.sources.BaseTextSource;
import com.xored.glance.ui.sources.ColorManager;
import com.xored.glance.ui.sources.ITextBlock;
import com.xored.glance.ui.sources.ITextSourceListener;
import com.xored.glance.ui.sources.Match;
import com.xored.glance.ui.sources.SourceSelection;

/**
 * @author Yuri Strot
 * 
 */
public class SourceViewerControl extends BaseTextSource implements
		ISelectionChangedListener {

	public static String ANNOTATION_TYPE = ColorManager.ANNOTATION_ID;

	public static String SELECTED_ANNOTATION_TYPE = ColorManager.ANNOTATION_SELECTED_ID;
	   
	public SourceViewerControl(SourceViewer viewer) {
		this.viewer = viewer;
		listeners = new ListenerList();
		blocks = new TextViewerBlock[] { new TextViewerBlock(viewer) };
		viewer.addSelectionChangedListener(this);
	}

	public void addTextSourceListener(ITextSourceListener listener) {
		listeners.add(listener);
	}

	public void removeTextSourceListener(ITextSourceListener listener) {
		listeners.add(listener);
	}

	public void dispose() {
		if (!disposed) {
		    if (selected != null){
		        selectText(selected);
		    }
		    select(null);
			viewer.removeSelectionChangedListener(this);
			replaceMatches(Match.EMPTY);
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

	public TextViewerBlock getBlock() {
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
	    Annotation[] remove = getAnnotations(true);
        Map<Annotation, Position> add = match != null ? createAnnotations(new Match[] { match }, true) 
            : new HashMap<Annotation, Position>();
        IAnnotationModel model = viewer.getAnnotationModel();
        if (model instanceof IAnnotationModelExtension) {
            IAnnotationModelExtension eModel = (IAnnotationModelExtension) model;
            eModel.replaceAnnotations(remove, add);
        } else {
            for (Annotation annotation : remove) {
                model.removeAnnotation(annotation);
            }
            for (Annotation annotation : add.keySet()) {
                model.addAnnotation(annotation, add.get(annotation));
            }
        }
        
        if (match != null){
            viewer.revealRange(match.getOffset(), match.getLength());
        }
        selected = match;
	}

	public void show(Match[] matches) {
		replaceMatches(matches);
	}

	private void replaceMatches(Match[] matches) {
		Annotation[] remove = getAnnotations(false);
		Map<Annotation, Position> add = createAnnotations(matches, false);
		IAnnotationModel model = viewer.getAnnotationModel();
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension eModel = (IAnnotationModelExtension) model;
			eModel.replaceAnnotations(remove, add);
		} else {
			for (Annotation annotation : remove) {
				model.removeAnnotation(annotation);
			}
			for (Annotation annotation : add.keySet()) {
				model.addAnnotation(annotation, add.get(annotation));
			}
		}
	}

	private Map<Annotation, Position> createAnnotations(Match[] matches, boolean selected) {
		Map<Annotation, Position> map = new HashMap<Annotation, Position>();
		for (Match match : matches) {
			Annotation annotation = new Annotation(selected ? SELECTED_ANNOTATION_TYPE 
			    : ANNOTATION_TYPE, false, null);
			Position position = new Position(match.getOffset(), match
					.getLength());
			map.put(annotation, position);
		}
		return map;
	}

	private Annotation[] getAnnotations(boolean selected) {
	    String type = selected ? SELECTED_ANNOTATION_TYPE : ANNOTATION_TYPE;
		IAnnotationModel model = viewer.getAnnotationModel();
		List<Annotation> annotations = new ArrayList<Annotation>();
		if (model != null) {
			Iterator<?> it = model.getAnnotationIterator();
			while (it.hasNext()) {
				Annotation annotation = (Annotation) it.next();
				if (type.equals(annotation.getType())) {
					annotations.add(annotation);
				}
			}
		}
		return annotations.toArray(new Annotation[annotations.size()]);
	}

	private final ListenerList listeners;
	private boolean disposed;
	private final TextViewerBlock[] blocks;
	private final SourceViewer viewer;
	private Match selected;
}

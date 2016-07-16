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

import com.xored.glance.ui.controls.text.styled.TextSelector;
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
public class SourceViewerControl extends BaseTextSource implements ISelectionChangedListener {

	public static String ANNOTATION_TYPE = ColorManager.ANNOTATION_ID;

	public static String SELECTED_ANNOTATION_TYPE = ColorManager.ANNOTATION_SELECTED_ID;

	public SourceViewerControl(final SourceViewer viewer) {
		this.viewer = viewer;
		listeners = new ListenerList<ITextSourceListener>();
		blocks = new TextViewerBlock[] { new TextViewerBlock(viewer) };
	}

	public void addTextSourceListener(final ITextSourceListener listener) {
		listeners.add(listener);
	}

	public void removeTextSourceListener(final ITextSourceListener listener) {
		listeners.add(listener);
	}

	public void dispose() {
		if (!disposed) {
			selector.dispose();
			select(null);
			viewer.removeSelectionChangedListener(this);
			replaceMatches(Match.EMPTY);
			getBlock().dispose();
			disposed = true;
		}
	}

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
		final Point selection = viewer.getSelectedRange();
		return new SourceSelection(getBlock(), selection.x, selection.y);
	}

	public void select(final Match match) {
		final Annotation[] remove = getAnnotations(true);
		final Map<Annotation, Position> add = match != null ? createAnnotations(
				new Match[] { match }, true)
				: new HashMap<Annotation, Position>();
		final IAnnotationModel model = viewer.getAnnotationModel();
		if (model instanceof IAnnotationModelExtension) {
			final IAnnotationModelExtension eModel = (IAnnotationModelExtension) model;
			eModel.replaceAnnotations(remove, add);
		} else {
			for (final Annotation annotation : remove) {
				model.removeAnnotation(annotation);
			}
			for (final Annotation annotation : add.keySet()) {
				model.addAnnotation(annotation, add.get(annotation));
			}
		}

		selector.setMatch(match);
	}

	public void show(final Match[] matches) {
		replaceMatches(matches);
	}

	private void replaceMatches(final Match[] matches) {
		final Annotation[] remove = getAnnotations(false);
		final Map<Annotation, Position> add = createAnnotations(matches, false);
		final IAnnotationModel model = viewer.getAnnotationModel();
		if (model instanceof IAnnotationModelExtension) {
			final IAnnotationModelExtension eModel = (IAnnotationModelExtension) model;
			eModel.replaceAnnotations(remove, add);
		} else {
			for (final Annotation annotation : remove) {
				model.removeAnnotation(annotation);
			}
			for (final Annotation annotation : add.keySet()) {
				model.addAnnotation(annotation, add.get(annotation));
			}
		}
	}

	private Map<Annotation, Position> createAnnotations(final Match[] matches,
			final boolean selected) {
		final Map<Annotation, Position> map = new HashMap<Annotation, Position>();
		for (final Match match : matches) {
			final Annotation annotation = new Annotation(
					selected ? SELECTED_ANNOTATION_TYPE : ANNOTATION_TYPE,
					false, null);
			final Position position = new Position(match.getOffset(),
					match.getLength());
			map.put(annotation, position);
		}
		return map;
	}

	private Annotation[] getAnnotations(final boolean selected) {
		final String type = selected ? SELECTED_ANNOTATION_TYPE
				: ANNOTATION_TYPE;
		final IAnnotationModel model = viewer.getAnnotationModel();
		final List<Annotation> annotations = new ArrayList<Annotation>();
		if (model != null) {
			final Iterator<?> it = model.getAnnotationIterator();
			while (it.hasNext()) {
				final Annotation annotation = (Annotation) it.next();
				if (type.equals(annotation.getType())) {
					annotations.add(annotation);
				}
			}
		}
		return annotations.toArray(new Annotation[annotations.size()]);
	}

	@Override
	public void init() {
		selector = new ViewerSelector(viewer);
		viewer.addSelectionChangedListener(this);
	}

	private TextSelector selector;
	private final ListenerList<ITextSourceListener> listeners;
	private boolean disposed;
	private final TextViewerBlock[] blocks;
	private final SourceViewer viewer;
}

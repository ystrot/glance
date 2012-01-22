/**
 * 
 */
package com.xored.glance.ui.viewers.descriptors;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Control;

import com.xored.glance.internal.ui.viewers.SourceViewerControl;
import com.xored.glance.ui.sources.ITextSource;

/**
 * @author Yuri Strot
 * 
 */
public class SourceViewerDescriptor extends AbstractViewerDescriptor {

	public boolean isValid(Control control) {
		ITextViewer viewer = getTextViewer(control);
		if (viewer instanceof SourceViewer) {
			SourceViewer sViewer = (SourceViewer) viewer;
			if (sViewer.getAnnotationModel() != null
					&& sViewer.getDocument() != null)
				return true;
		}
		return false;
	}

	public ITextSource createSource(Control control) {
		return new SourceViewerControl((SourceViewer) getTextViewer(control));
	}
}

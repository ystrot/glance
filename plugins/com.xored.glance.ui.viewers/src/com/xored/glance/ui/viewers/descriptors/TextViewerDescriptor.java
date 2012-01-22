/**
 * 
 */
package com.xored.glance.ui.viewers.descriptors;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Control;

import com.xored.glance.internal.ui.viewers.TextViewerControl;
import com.xored.glance.ui.sources.ITextSource;

/**
 * @author Yuri Strot
 * 
 */
public class TextViewerDescriptor extends AbstractViewerDescriptor {

	public boolean isValid(Control control) {
		ITextViewer viewer = getTextViewer(control);
		return viewer instanceof TextViewer && viewer.getDocument() != null;
	}

	public ITextSource createSource(Control control) {
		return new TextViewerControl((TextViewer) getTextViewer(control));
	}
}

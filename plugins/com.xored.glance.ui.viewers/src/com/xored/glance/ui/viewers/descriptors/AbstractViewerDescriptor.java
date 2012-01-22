/**
 * 
 */
package com.xored.glance.ui.viewers.descriptors;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import com.xored.glance.ui.sources.ITextSourceDescriptor;
import com.xored.glance.ui.viewers.utils.ViewerUtils;

/**
 * @author Yuri Strot
 * 
 */
public abstract class AbstractViewerDescriptor implements ITextSourceDescriptor {

	protected ITextViewer getTextViewer(Control control) {
		if (control instanceof StyledText) {
			return getTextViewer((StyledText) control);
		}
		return null;
	}

	protected ITextViewer getTextViewer(StyledText text) {
		return ViewerUtils.getTextViewer(text);
	}

}

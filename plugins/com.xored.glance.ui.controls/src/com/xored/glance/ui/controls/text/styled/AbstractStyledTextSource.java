/**
 * 
 */
package com.xored.glance.ui.controls.text.styled;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
public abstract class AbstractStyledTextSource extends BaseTextSource implements
		SelectionListener {

	public AbstractStyledTextSource(StyledText text) {
		this.text = text;
		blocks = new StyledTextBlock[] { createTextBlock() };
		list = new ListenerList();
		text.addSelectionListener(this);
	}

	protected StyledTextBlock createTextBlock() {
		return new StyledTextBlock(text);
	}

	public final void dispose() {
		if (text != null && !text.isDisposed() && !disposed) {
			doDispose();
		}
		disposed = true;
	}

	protected void doDispose() {
	    if (selected != null) {
	        text.setSelection(selected.getOffset(), selected.getOffset()
	           + selected.getLength());
	        select(null);
	    } else {
	        int x = text.getSelection().x;
	        text.setSelection(x, x);
	    }
		text.removeSelectionListener(this);
	}

	public boolean isDisposed() {
		return disposed;
	}

	public ITextBlock[] getBlocks() {
		return blocks;
	}

	public void addTextSourceListener(ITextSourceListener listener) {
		list.add(listener);
	}

	public void removeTextSourceListener(ITextSourceListener listener) {
		list.remove(listener);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		fireSelectionChanged();
	}

	public void widgetSelected(SelectionEvent e) {
		fireSelectionChanged();
	}

	private void fireSelectionChanged() {
		SourceSelection selection = getSelection();
		Object[] objects = list.getListeners();
		for (Object object : objects) {
			ITextSourceListener listener = (ITextSourceListener) object;
			listener.selectionChanged(selection);
		}
	}

	public SourceSelection getSelection() {
		Point point = text.getSelection();
		SourceSelection selection = new SourceSelection(blocks[0], point.x,
				point.y - point.x);
		return selection;
	}

	public void select(Match match) {
        this.selected = match;
	}

	protected StyledText getText() {
		return text;
	}

	private final StyledText text;

	private boolean disposed;
	private final ListenerList list;
	private final StyledTextBlock[] blocks;
	protected Match selected;
}

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
public abstract class AbstractStyledTextSource extends BaseTextSource implements SelectionListener {

    public AbstractStyledTextSource(final StyledText text) {
        this.text = text;
        blocks = new StyledTextBlock[] { createTextBlock() };
        list = new ListenerList();
    }

    protected StyledTextBlock createTextBlock() {
        return new StyledTextBlock(text);
    }

    @Override
    public final void dispose() {
        if (text != null && !text.isDisposed() && !disposed) {
            doDispose();
        }
        disposed = true;
    }

    protected void doDispose() {
        if (selected != null) {
            text.setSelection(selected.getOffset(), selected.getOffset() + selected.getLength());
            select(null);
        } else {
            final int x = text.getSelection().x;
            text.setSelection(x, x);
        }
        text.removeSelectionListener(this);
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public ITextBlock[] getBlocks() {
        return blocks;
    }

    @Override
    public void addTextSourceListener(final ITextSourceListener listener) {
        list.add(listener);
    }

    @Override
    public void removeTextSourceListener(final ITextSourceListener listener) {
        list.remove(listener);
    }

    @Override
    public void widgetDefaultSelected(final SelectionEvent e) {
        fireSelectionChanged();
    }

    @Override
    public void widgetSelected(final SelectionEvent e) {
        fireSelectionChanged();
    }

    private void fireSelectionChanged() {
        final SourceSelection selection = getSelection();
        final Object[] objects = list.getListeners();
        for (final Object object : objects) {
            final ITextSourceListener listener = (ITextSourceListener) object;
            listener.selectionChanged(selection);
        }
    }

    @Override
    public SourceSelection getSelection() {
        final Point point = text.getSelection();
        final SourceSelection selection = new SourceSelection(blocks[0], point.x, point.y - point.x);
        return selection;
    }

    @Override
    public void select(final Match match) {
        this.selected = match;
    }

    protected StyledText getText() {
        return text;
    }

    @Override
    public void init() {
        if (text.getSelectionCount() > 0) {
            text.setSelection(text.getSelection().x + text.getSelectionCount());
        }
        text.addSelectionListener(this);
    }

    private final StyledText text;

    private boolean disposed;
    private final ListenerList list;
    private final StyledTextBlock[] blocks;
    protected Match selected;
}

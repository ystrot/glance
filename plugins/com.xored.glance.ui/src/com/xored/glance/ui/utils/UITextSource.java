/**
 * 
 */
package com.xored.glance.ui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.widgets.Control;

import com.xored.glance.ui.sources.ITextBlock;
import com.xored.glance.ui.sources.ITextSource;
import com.xored.glance.ui.sources.ITextSourceListener;
import com.xored.glance.ui.sources.Match;
import com.xored.glance.ui.sources.SourceSelection;

/**
 * @author Yuri Strot
 * 
 */
public class UITextSource implements ITextSource, ITextSourceListener {

	public UITextSource(ITextSource source, Control control) {
		this.source = source;
		this.control = control;

		blocks = new ArrayList<UITextBlock>();
		blockToBlock = new HashMap<ITextBlock, UITextBlock>();
		listeners = new ListenerList();
		source.addTextSourceListener(this);
		addBlocks(source.getBlocks());
		updateSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xored.glance.ui.sources.ITextSource#getSelection()
	 */
	public SourceSelection getSelection() {
		return selection;
	}

	public boolean isIndexRequired() {
		return source.isIndexRequired();
	}

	public void dispose() {
		synchronized (blocks) {
			for (UITextBlock block : blocks) {
				block.dispose();
			}
		}
		source.removeTextSourceListener(this);
		source.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xored.glance.ui.sources.ITextSource#isDisposed()
	 */
	public boolean isDisposed() {
		return source.isDisposed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xored.glance.ui.sources.ITextSource#getBlocks()
	 */
	public ITextBlock[] getBlocks() {
		return blocks.toArray(new ITextBlock[blocks.size()]);
	}

	public void index(final IProgressMonitor monitor) {
		source.index(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xored.glance.ui.sources.ITextSource#select(com.xored.glance.ui.sources
	 * .Match)
	 */
	public void select(final Match match) {
		UIUtils.asyncExec(control, new Runnable() {

			public void run() {
				if (!source.isDisposed()) {
					if (match == null)
						source.select(null);
					else {
						UITextBlock block = (UITextBlock) match.getBlock();
						source.select(new Match(block.getBlock(), match
								.getOffset(), match.getLength()));
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xored.glance.ui.sources.ITextSource#show(com.xored.glance.ui.sources
	 * .Match[])
	 */
	public void show(final Match[] matches) {
		UIUtils.asyncExec(control, new Runnable() {

			public void run() {
				if (!source.isDisposed()) {
					Match[] newMatches = new Match[matches.length];
					for (int i = 0; i < matches.length; i++) {
						Match match = matches[i];
						UITextBlock block = (UITextBlock) match.getBlock();
						newMatches[i] = new Match(block.getBlock(), match
								.getOffset(), match.getLength());
					}
					source.show(newMatches);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xored.glance.ui.sources.ITextSource#addTextSourceListener(com.xored
	 * .glance.ui.sources.ITextSourceListener)
	 */
	public void addTextSourceListener(ITextSourceListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xored.glance.ui.sources.ITextSource#removeTextSourceListener(com.
	 * xored.glance.ui.sources.ITextSourceListener)
	 */
	public void removeTextSourceListener(ITextSourceListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xored.glance.ui.sources.ITextSourceListener#blocksChanged(com.xored
	 * .glance.ui.sources.ITextBlock[],
	 * com.xored.glance.ui.sources.ITextBlock[])
	 */
	public void blocksChanged(ITextBlock[] removed, ITextBlock[] added) {
		ITextBlock[] uiRemoved = removeBlocks(removed);
		ITextBlock[] uiAdded = addBlocks(added);
		Object[] objects = listeners.getListeners();
		for (Object object : objects) {
			ITextSourceListener listener = (ITextSourceListener) object;
			listener.blocksChanged(uiRemoved, uiAdded);
		}
	}

	public void blocksReplaced(ITextBlock[] newBlocks) {
		synchronized (this.blocks) {
			for (UITextBlock uiBlock : blockToBlock.values()) {
				uiBlock.dispose();
			}
			blockToBlock = new HashMap<ITextBlock, UITextBlock>();
			blocks = new ArrayList<UITextBlock>();
		}
		ITextBlock[] uiAdded = addBlocks(newBlocks);
		Object[] objects = listeners.getListeners();
		for (Object object : objects) {
			ITextSourceListener listener = (ITextSourceListener) object;
			listener.blocksReplaced(uiAdded);
		}
		selection = source.getSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xored.glance.ui.sources.ITextSourceListener#selectionChanged(com.
	 * xored.glance.ui.sources.SourceSelection)
	 */
	public void selectionChanged(SourceSelection selection) {
		SourceSelection newSelection = updateSelection();
		Object[] objects = listeners.getListeners();
		for (Object object : objects) {
			ITextSourceListener listener = (ITextSourceListener) object;
			listener.selectionChanged(newSelection);
		}
	}

	protected ITextBlock[] addBlocks(ITextBlock[] blocks) {
		synchronized (this.blocks) {
			ITextBlock[] added = new ITextBlock[blocks.length];
			for (int i = 0; i < blocks.length; i++) {
				ITextBlock block = blocks[i];
				UITextBlock uiBlock = new UITextBlock(block);
				added[i] = uiBlock;
				this.blocks.add(uiBlock);
				blockToBlock.put(block, uiBlock);
			}
			return added;
		}
	}

	protected ITextBlock[] removeBlocks(ITextBlock[] blocks) {
		synchronized (this.blocks) {
			List<ITextBlock> removed = new ArrayList<ITextBlock>(blocks.length);
			for (int i = 0; i < blocks.length; i++) {
				ITextBlock block = blocks[i];
				UITextBlock uiBlock = blockToBlock.remove(block);
				if (uiBlock != null) {
					removed.add(uiBlock);
					this.blocks.remove(uiBlock);
					uiBlock.dispose();
				}
			}
			return removed.toArray(new ITextBlock[removed.size()]);
		}
	}

	protected SourceSelection updateSelection() {
		SourceSelection sourceSelection = source.getSelection();
		if (sourceSelection == null) {
			selection = null;
		} else {
			selection = new SourceSelection(blockToBlock.get(sourceSelection
					.getBlock()), sourceSelection.getOffset(), sourceSelection
					.getLength());
		}
		return selection;
	}

	private SourceSelection selection;
	private Map<ITextBlock, UITextBlock> blockToBlock;
	private ListenerList listeners;
	private List<UITextBlock> blocks;
	private ITextSource source;
	private Control control;

}

package com.xored.glance.internal.ui.panels;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.xored.glance.internal.ui.GlancePlugin;

/**
 * Workaround disposed widget in org.eclipse.jface.action.StatusLineManager#update
 * @author Shinji Kashihara
 */
public class StatusLineManagerProxy extends StatusLineManager {

	private StatusLineManager origin;
	
	public StatusLineManagerProxy(StatusLineManager origin) {
		this.origin = origin;
		
		// See add method
		add(new GroupMarker(BEGIN_GROUP));
		add(new GroupMarker(MIDDLE_GROUP));
		add(new GroupMarker(END_GROUP));
	}

	public void add(IContributionItem item) {
		// Parent constructor called before initialize origin field
		if (origin != null) {
			origin.add(item);
		}
	}

	public void update(boolean force) {

		// origin.update(force);

		if (isDirty() || force) {
			if (statusLineExist()) {
				Composite statusLine = getStatusLine();
				statusLine.setRedraw(false);
				Control ws[] = statusLine.getChildren();
				for (int i = 0; i < ws.length; i++) {
					Control w = ws[i];

					// Workaround disposed widget in org.eclipse.jface.action.StatusLineManager#update
					if (w.isDisposed()) {
						String message = "Status line item was disposed. " + w.getClass().getName();
						if (w instanceof CLabel) {
							message += " " + ((CLabel) w).getText();
						}
						GlancePlugin.info(message, new IllegalStateException(message));
						continue;
					}

					Object data = w.getData();
					if (data instanceof IContributionItem) {
						w.dispose();
					}
				}
				int oldChildCount = statusLine.getChildren().length;
				IContributionItem[] items = getItems();
				for (int i = 0; i < items.length; ++i) {
					IContributionItem ci = items[i];
					if (ci.isVisible()) {
						ci.fill(statusLine);
						Control[] newChildren = statusLine.getChildren();
						for (int j = oldChildCount; j < newChildren.length; j++) {
							newChildren[j].setData(ci);
						}
						oldChildCount = newChildren.length;
					}
				}
				setDirty(false);
				statusLine.layout();
				statusLine.setRedraw(true);
			}
		}
	}

	private boolean statusLineExist() {
		Composite statusLine = getStatusLine();
		return statusLine != null && !statusLine.isDisposed();
	}
    
    private Composite getStatusLine() {
    	return (Composite) getControl();
    }

    //-------------------------------------------------------------------------------------------
	// Following methods delegate only
    
	public int hashCode() {
		return origin.hashCode();
	}

	public void add(IAction action) {
		origin.add(action);
	}

	public Control createControl(Composite parent) {
		return origin.createControl(parent);
	}

	public Control createControl(Composite parent, int style) {
		return origin.createControl(parent, style);
	}

	public void dispose() {
		origin.dispose();
	}

	public boolean equals(Object obj) {
		return origin.equals(obj);
	}

	public void appendToGroup(String groupName, IAction action) {
		origin.appendToGroup(groupName, action);
	}

	public void appendToGroup(String groupName, IContributionItem item) {
		origin.appendToGroup(groupName, item);
	}

	public Control getControl() {
		return origin.getControl();
	}

	public IProgressMonitor getProgressMonitor() {
		return origin.getProgressMonitor();
	}

	public IContributionItem find(String id) {
		return origin.find(id);
	}

	public IContributionItem[] getItems() {
		return origin.getItems();
	}

	public int getSize() {
		return origin.getSize();
	}

	public IContributionManagerOverrides getOverrides() {
		return origin.getOverrides();
	}

	public boolean isCancelEnabled() {
		return origin.isCancelEnabled();
	}

	public void setCancelEnabled(boolean enabled) {
		origin.setCancelEnabled(enabled);
	}

	public void setErrorMessage(String message) {
		origin.setErrorMessage(message);
	}

	public void setErrorMessage(Image image, String message) {
		origin.setErrorMessage(image, message);
	}

	public void setMessage(String message) {
		origin.setMessage(message);
	}

	public void setMessage(Image image, String message) {
		origin.setMessage(image, message);
	}

	public int indexOf(String id) {
		return origin.indexOf(id);
	}

	public void insert(int index, IContributionItem item) {
		origin.insert(index, item);
	}

	public void insertAfter(String ID, IAction action) {
		origin.insertAfter(ID, action);
	}

	public void insertAfter(String ID, IContributionItem item) {
		origin.insertAfter(ID, item);
	}

	public void insertBefore(String ID, IAction action) {
		origin.insertBefore(ID, action);
	}

	public String toString() {
		return origin.toString();
	}

	public void insertBefore(String ID, IContributionItem item) {
		origin.insertBefore(ID, item);
	}

	public boolean isDirty() {
		return origin.isDirty();
	}

	public boolean isEmpty() {
		return origin.isEmpty();
	}

	public void markDirty() {
		origin.markDirty();
	}

	public void prependToGroup(String groupName, IAction action) {
		origin.prependToGroup(groupName, action);
	}

	public void prependToGroup(String groupName, IContributionItem item) {
		origin.prependToGroup(groupName, item);
	}

	public IContributionItem remove(String ID) {
		return origin.remove(ID);
	}

	public IContributionItem remove(IContributionItem item) {
		return origin.remove(item);
	}

	public void removeAll() {
		origin.removeAll();
	}

	public boolean replaceItem(String identifier, IContributionItem replacementItem) {
		return origin.replaceItem(identifier, replacementItem);
	}

	public void setOverrides(IContributionManagerOverrides newOverrides) {
		origin.setOverrides(newOverrides);
	}
}

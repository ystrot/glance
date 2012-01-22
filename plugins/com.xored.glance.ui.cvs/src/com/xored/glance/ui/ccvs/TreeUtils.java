/*******************************************************************************
 * Copyright (c) 2012 xored software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     xored software, Inc. - initial API and implementation (Ivan Lobachev)
 ******************************************************************************/
package com.xored.glance.ui.ccvs;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryRoot;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.progress.PendingUpdateAdapter;

@SuppressWarnings("restriction")
public class TreeUtils {

	public static String getFullPath(TreeItem item) {
		String res = "";
		TreeItem curItem = item;
		while (curItem != null) {
			String itemName;
			if (curItem.getData() instanceof RemoteFile) {
				itemName = ((RemoteFile) (curItem.getData())).getName();
			} else {
				itemName = curItem.getText();
			}
			if (res.length() == 0) {
				res = itemName;
			} else {
				res = itemName + "/" + res;
			}

			curItem = curItem.getParentItem();
		}
		return res;
	}

	public static String getTreeItemName(TreeItem treeItem) {
		if (treeItem.getData() instanceof RemoteFolder) {
			return ((RemoteFolder) treeItem.getData()).getName();
		} else if (treeItem.getData() instanceof RepositoryRoot) {
			return ((RepositoryRoot) treeItem.getData()).getRoot().toString();
		} else if (treeItem.getData() instanceof RemoteFile) {
			return ((RemoteFile) treeItem.getData()).getName();
		} else if (treeItem.getData() instanceof CVSTag) {
			return ((CVSTag) treeItem.getData()).getName();
		}
		return treeItem.getText();
	}

	public static TreeItem findTreeItem(Tree tree, String path, TreeItem item) {
		TreeItem[] items;
		if (item == null) {
			items = tree.getItems();
		} else {
			items = item.getItems();
		}
		if (path == null) {
			return item;
		}
		for (TreeItem treeItem : items) {
			String curFolder = TreeUtils.getTreeItemName(treeItem);
			if (treeItem.getData() != null
					&& path.startsWith(curFolder)
					&& ((path.length() == curFolder.length()) || (path
							.charAt(curFolder.length()) == '/'))) {
				String tPath = path.replace(curFolder, "");
				if (tPath != null && tPath.length() > 0) {
					tPath = (tPath.charAt(0) == '/') ? tPath.substring(1)
							: tPath;
				} else {
					tPath = null;
				}
				return findTreeItem(tree, tPath, treeItem);
			}
		}
		return null;
	}

	/**
	 * Expand Tree up to the element specified by <path>
	 * 
	 * @param treeViewer
	 * @param path
	 * @param item
	 * @param treeExpandUpdater
	 */
	public static void expandElement(final TreeViewer treeViewer, String path,
			final TreeItem item, ITreeExpandUpdater treeExpandUpdater) {
		TreeItem[] items;
		if (item == null) {
			items = treeViewer.getTree().getItems();
		} else {
			if (item != null && !(item.getData() instanceof RemoteFile)
					&& !item.getExpanded()) {
				treeViewer.expandToLevel(item.getData(), 1);
				treeExpandUpdater.updateOnExpand(item);
				if (item.getItems().length > 0 && path != null) {
					if (item.getItems()[0].getData() instanceof PendingUpdateAdapter) {
						item.getItems()[0]
								.addDisposeListener(new PendingDisposeListener(
										treeViewer, path, item,
										treeExpandUpdater));
					} else {
						expandElement(treeViewer, path, item, treeExpandUpdater);
					}
					return;
				}
			}
			items = item.getItems();
		}

		if (path == null) {
			// treeViewer.getTree().setSelection(item);

			Utils.asyncExec(new Runnable() {
				public void run() {
					treeViewer.getTree().setSelection(item);
				}
			}, treeViewer);
			// System.out.println("select: " + item.getText());
			// textSource.setMatch(textSource.newMatch);
			return;
		}
		for (TreeItem treeItem : items) {
			String curFolder = TreeUtils.getTreeItemName(treeItem);
			if (treeItem.getData() != null
					&& path.startsWith(curFolder)
					&& ((path.length() == curFolder.length()) || (path
							.charAt(curFolder.length()) == '/'))) {
				String tPath = path.replace(curFolder, "");
				if (tPath != null && tPath.length() > 0) {
					tPath = (tPath.charAt(0) == '/') ? tPath.substring(1)
							: tPath;
				} else {
					tPath = null;
				}
				expandElement(treeViewer, tPath, treeItem, treeExpandUpdater);
				return;
			}
		}
	}

}

class PendingDisposeListener implements DisposeListener {
	private final TreeViewer treeViewer;
	private final String path;
	private final TreeItem item;
	private final ITreeExpandUpdater treeExpandUpdater;

	public PendingDisposeListener(TreeViewer treeViewer, String path,
			TreeItem item, ITreeExpandUpdater treeExpandUpdater) {
		this.treeViewer = treeViewer;
		this.path = path;
		this.item = item;
		this.treeExpandUpdater = treeExpandUpdater;
	}

	public void widgetDisposed(DisposeEvent e) {
		TreeUtils.expandElement(this.treeViewer, this.path, this.item,
				this.treeExpandUpdater);
	}
}

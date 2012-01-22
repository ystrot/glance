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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryRoot;
import org.eclipse.ui.progress.PendingUpdateAdapter;

import com.xored.glance.ui.controls.tree.TreeCell;
import com.xored.glance.ui.controls.tree.content.TreeContent;
import com.xored.glance.ui.controls.tree.content.TreeItemContent;
import com.xored.glance.ui.controls.tree.content.TreeNode;

@SuppressWarnings("restriction")
public class CVSHistorySourceTree extends TreeContent implements
		ITreeExpandUpdater {
	private List<CVSHistoryTree> trees = new ArrayList<CVSHistoryTree>();
	private Tree tree;
	private TreeListener listener;

	public CVSHistorySourceTree(Tree tree) {
		this.tree = tree;
		init();
	}

	@Override
	public void dispose() {
		if (listener != null && !tree.isDisposed()) {
			tree.removeTreeListener(listener);
			listener = null;
		}
	}

	@Override
	public TreeItemContent getContent(TreeCell cell) {
		TreeItem item = cell.getTreeItem();
		Object node = treeItemToCVSNode.get(item);
		if (node == null) {
			node = findCVSTreeElement(item);
		}
		TreeItemContent content = nodeToItemContent.get(node);
		return content;
	}

	public Map<ICVSHistoryNode, TreeItemContent> nodeToItemContent = new HashMap<ICVSHistoryNode, TreeItemContent>();

	public TreeNode buildGlanceTree(ICVSHistoryNode parent, boolean foldersOnly) {
		if (parent.getCount() != 1) {
			return null;
		}
		TreeNode root = new TreeNode(null);
		TreeItemContent rootContent = new TreeItemContent(root, parent
				.getElementName(), 0);
		nodeToItemContent.put(parent, rootContent);
		for (ICVSHistoryNode node : parent.getNodeChildren()) {
			if (node.getCount() == 1) {
				if (node.getType() == ICVSHistoryNode.FILE && foldersOnly) {
					continue;
				}
				TreeNode childTree = buildGlanceTree(node, foldersOnly);
				root.add(new TreeNode[] { childTree });
			}
		}
		return root;
	}

	@Override
	public void index(final IProgressMonitor monitor) {

		tree.getDisplay().asyncExec(new Runnable() {

			public void run() {
				trees = new ArrayList<CVSHistoryTree>();
				final TreeItem[] items = tree.getItems();
				if (items.length <= 0) {
					return;
				}

				final boolean isHeadTreeOnly = (items[0].getData() instanceof RemoteFolder);
				final RepositoryRoot[] repositories = isHeadTreeOnly ? new RepositoryRoot[1]
						: new RepositoryRoot[items.length];

				for (int i = 0; i < repositories.length; i++) {
					if (items[i].getData() instanceof RepositoryRoot) {
						repositories[i] = ((RepositoryRoot) (items[i].getData()));
					} else if (items[i].getData() instanceof RemoteFolder) {
						repositories[i] = new RepositoryRoot(
								((RemoteFolder) (items[i].getData()))
										.getRepository());
					}
				}

				new Thread() {
					// tree.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						// monitor.beginTask("", folders.length);
						monitor.beginTask("", repositories.length);
						// for (final ICVSRemoteFolder folder : folders) {
						for (final RepositoryRoot repoRoot : repositories) {
							SubProgressMonitor subMonitor = new SubProgressMonitor(
									monitor, 1);
							HistoryFetcher fetcher = new HistoryFetcher();
							try {

								final CVSHistoryTree historyTree = fetcher
										.fetchHistory(subMonitor, /* folder */
										repoRoot.getRemoteFolder("",
												new CVSTag(),
												new NullProgressMonitor()));

								TreeNode repoTree = null;
								if (isHeadTreeOnly) {
									ICVSHistoryNode[] nodes = historyTree
											.getChild("HEAD").getNodeChildren();
									for (final ICVSHistoryNode node : nodes) {
										if (node.getCount() != 1) {
											continue;
										}
										repoTree = buildGlanceTree(node, true);
										add(new TreeNode[] { repoTree });
										tree.getDisplay().asyncExec(
												new Runnable() {

													public void run() {
														cacheCVSNode(
																items,
																node
																		.getElementName(),
																node);
													}
												});

									}
								} else {
									repoTree = buildGlanceTree(historyTree,
											false);
									add(new TreeNode[] { repoTree });
								}

								tree.getDisplay().asyncExec(new Runnable() {

									public void run() {
										cacheCVSNode(items, repoRoot.getRoot()
												.toString(), historyTree);
									}
								});
								trees.add(historyTree);
							} catch (OperationCanceledException e) {
								// ignore this exception
							} catch (CVSException e) {
								e.printStackTrace();
							}
						}
						monitor.done();
					}
				}.start();
				// });

			}
		});

	}

	private void cacheCVSNode(TreeItem[] items, String elementName,
			ICVSHistoryNode node) {
		for (TreeItem item : items) {
			if (item.getText().equals(elementName)) {
				treeItemToCVSNode.put(item, node);
			}
		}

	}

	public Map<TreeItem, ICVSHistoryNode> treeItemToCVSNode = new HashMap<TreeItem, ICVSHistoryNode>();

	@SuppressWarnings("restriction")
	private ICVSHistoryNode findCVSTreeElement(TreeItem item) {
		Object data = item.getData();
		if (data == null || data instanceof PendingUpdateAdapter) {
			return null;
		}
		String fullPath = TreeUtils.getFullPath(item);
		CVSHistoryTree curCVSTree = null;
		for (CVSHistoryTree cvsTree : trees) {
			if (fullPath.startsWith(cvsTree.getRepository().toString())) {
				curCVSTree = cvsTree;
				break;
			}
		}
		if (curCVSTree != null) {
			return findInRepo(curCVSTree, item.getData(), fullPath);
		} else {
			for (CVSHistoryTree cvsTree : trees) {
				ICVSHistoryNode node = findInRepo(cvsTree, item.getData(),
						cvsTree.getRepository().toString() + "/" + "HEAD/"
								+ fullPath);
				if (node != null) {
					return node;
				}
			}
		}
		return null;
	}

	private ICVSHistoryNode findInRepo(CVSHistoryTree curCVSTree,
			Object itemData, String fullPath) {
		if (curCVSTree == null) {
			return null;
		}
		if (itemData instanceof RemoteFile) {
			int ind = fullPath.lastIndexOf("/");
			if (ind != -1) {
				return curCVSTree.findFile(fullPath.substring(ind + 1),
						fullPath.substring(0, ind));
			}
		} else {
			return curCVSTree.findFolder(fullPath);
		}
		return null;
	}

	/**
	 * Fills cvsElemsToTree map which associate ICVSHistoryTreeElement with
	 * TreeItee and updates Tree (highlighting, selection)
	 * 
	 * @param item
	 */
	@SuppressWarnings("restriction")
	public void updateOnExpand(final TreeItem item) {
		Object data = item.getData();
		if (data != null && data instanceof RepositoryRoot
				&& treeItemToCVSNode.get(item) == null) {
			for (CVSHistoryTree tree : trees) {
				if (tree.getRepository().equals(
						((RepositoryRoot) data).getRoot())) {
					treeItemToCVSNode.put(item, tree);
					// System.out.println("Added: " + item);
				}
			}
		}
		TreeItem[] items = item.getItems();
		if (items.length > 0
				&& items[0].getData() instanceof PendingUpdateAdapter) {
			items[0].addDisposeListener(new DisposeListener() {

				public void widgetDisposed(DisposeEvent e) {
					cacheItemChildren(((TreeItem) e.getSource())
							.getParentItem());
				}
			});
			return;
		} else if (items.length > 0) {
			cacheItemChildren(item);
		}
	}

	public void cacheItemChildren(TreeItem item) {
		TreeItem[] items = item.getItems();
		for (TreeItem treeItem : items) {
			treeItemToCVSNode.put(treeItem, findCVSTreeElement(treeItem));
			// System.out.println("Added: " + treeItem);
		}

	}

	@SuppressWarnings("restriction")
	private void init() {
		this.tree.addTreeListener(getListener());
	}

	private TreeListener getListener() {
		if (listener == null) {
			listener = new TreeListener() {

				public void treeCollapsed(TreeEvent event) {
					if (event.item instanceof TreeItem) {
					}
				}

				public void treeExpanded(TreeEvent event) {
					if (event.item != null && event.item instanceof TreeItem) {
						updateOnExpand((TreeItem) event.item);
					}
				}
			};
		}
		return listener;
	}
}

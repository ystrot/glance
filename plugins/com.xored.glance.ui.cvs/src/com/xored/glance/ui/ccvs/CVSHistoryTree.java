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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;

@SuppressWarnings("restriction")
public class CVSHistoryTree/* extends TreeContent */implements ICVSHistoryNode {
	// public static CVSHistoryTree tree;
	private final ICVSRepositoryLocation repository;
	private final CVSTag tag;
	private final Map<String, ICVSHistoryNode> elements;

	public CVSHistoryTree(ICVSRepositoryLocation repository, CVSTag tag) {
		if (tag == null)
			tag = new CVSTag();
		this.tag = tag;
		this.repository = repository;
		this.elements = new HashMap<String, ICVSHistoryNode>();
	}

	// @Override
	// public TreeItemContent getContent(TreeCell cell) {
	// return null;
	// }

	public ICVSRepositoryLocation getRepository() {
		return this.repository;
	}

	public ICVSHistoryNode[] getNodeChildren() {
		ICVSHistoryNode[] res = elements.values().toArray(
				new ICVSHistoryNode[0]);
		Arrays.sort(res, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((ICVSHistoryNode) arg0).getElementName().compareTo(
						((ICVSHistoryNode) arg1).getElementName());
			}
		});
		return res;
		// elements.values().toArray(new ICVSHistoryNode[0]);
	}

	public ICVSHistoryNode createFolder(String fullName) {
		int ind = fullName.indexOf("/");
		String curFolder = fullName;
		String childFolder = null;
		if (ind != -1) {
			curFolder = fullName.substring(0, ind);
			childFolder = fullName.substring(ind + 1);
		}

		ICVSHistoryNode elem = elements.get(curFolder);
		if (elem == null) {
			elem = new CVSHistoryElement(curFolder, ICVSHistoryNode.FOLDER,
					this, null);
			elements.put(curFolder, elem);
		}
		if (childFolder != null) {
			return elem.createFolder(childFolder);
		}
		return elem;
	}

	public ICVSHistoryNode findFolder(String fullName) {
		fullName = fullName.replace(getRepository().toString() + "/", "")
		/* .replace(tag.getName() + "/", "") */;
		int ind = fullName.indexOf("/");
		String curFolder = fullName;
		String childFolder = null;
		if (ind != -1) {
			curFolder = fullName.substring(0, ind);
			childFolder = fullName.substring(ind + 1);
		}
		ICVSHistoryNode elem = elements.get(curFolder);
		if (elem != null && elem.getCount() == 1) {
			if (childFolder == null) {
				return elem;
			}
			return elem.findFolder(childFolder);
		}
		return null;
	}

	public ICVSHistoryNode findFile(String fileName, String fullPath) {
		ICVSHistoryNode folder = findFolder(fullPath);
		if (folder != null) {
			ICVSHistoryNode elem = folder.findFile(fileName);
			if (elem != null && elem.getType() == ICVSHistoryNode.FILE
					&& elem.getCount() == 1) {
				return elem;
			}
		}
		return null;
	}

	public void createFile(String name, String fullPath, String revision) {
		ICVSHistoryNode folderElement = createFolder(fullPath);
		folderElement.createFile(name, revision);
	}

	public void removeFile(String name, String fullPath) {
		ICVSHistoryNode elem = createFolder(fullPath);
		if (elem != null) {
			elem.removeFile(name);
		}
	}

	public void accept(IHistoryTreeVisitor visitor) {
		Set<String> keys = elements.keySet();
		if (keys != null) {
			for (String key : keys) {
				elements.get(key).accept(visitor);
			}
		}
	}

	public RemoteFolderTree buildRemoteTree(final String elementName,
			boolean reset) {
		final HistoryTreeBuilder historyTreeBuilder = new HistoryTreeBuilder(
				repository, tag);
		if (reset) {
			historyTreeBuilder.reset();
		}
		ICVSHistoryNode elem = null;
		ICVSHistoryNode head = getChild(tag.getName());
		if (head != null) {
			elem = head.getChild(elementName);
		}
		if (elem != null) {
			elem.accept(new IHistoryTreeVisitor() {

				public boolean visit(ICVSHistoryNode element) {
					if (element.getType() == ICVSHistoryNode.FILE) {
						String elementPath = element.getFullPath().substring(
								repository.toString().length()
										+ tag.getName().length() + 2);

						historyTreeBuilder.newFile((new Path(elementPath
								.substring(elementName.length() + 1))),
								RemoteFile.create(elementPath, repository, tag,
										element.getRevision()));
						return false;
					}
					return true;
				}
			});
		}
		return historyTreeBuilder.getTree();
	}

	public RemoteFolderTree buildRemoteTree() {
		RemoteFolderTree tree = null;
		for (String elemName : elements.keySet()) {
			tree = buildRemoteTree(elemName, false);
		}
		return tree;
	}

	public void updateFileRevision(String name, String fullPath, String revision) {
		ICVSHistoryNode folderElement = createFolder(fullPath);
		ICVSHistoryNode elem = folderElement.findPhantomFile(name);
		if (elem == null) {
			folderElement.createFile(name, revision);
			elem = folderElement.findPhantomFile(name);
			elem.removed();
		}
		if (elem != null && elem.getType() == ICVSHistoryNode.FILE) {
			elem.updateRevision(revision);
		}
	}

	public List<ICVSHistoryNode> getAllElements() {
		Collection<ICVSHistoryNode> elems = elements.values();
		List<ICVSHistoryNode> finalCollection = new ArrayList<ICVSHistoryNode>();
		finalCollection.addAll(elems);
		for (ICVSHistoryNode elem : elems) {
			finalCollection.addAll(elem.getAllElements());
		}
		return finalCollection;
	}

	public String getElementName() {
		return repository.toString();
	}

	public String getFullPath() {
		return getElementName()/* + "/HEAD" */;
	}

	public ICVSHistoryNode getParent() {
		return null;
	}

	public int getType() {
		return ICVSHistoryNode.REPOSITORY;
	}

	public void added() {
	}

	public void createFile(String fileName, String revision) {
	}

	public ICVSHistoryNode findFile(String fileName) {
		return null;
	}

	public ICVSHistoryNode findPhantomFile(String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCount() {
		return 1;
	}

	public void removeFile(String name) {
	}

	public void removed() {
	}

	public String getText() {
		return getElementName();
	}

	public String getRevision() {
		return "";
	}

	public void setRevision(String revision) {
	}

	public void updateRevision(String revision) {
	}

	public ICVSHistoryNode getChild(String name) {
		return elements.get(name);
	}
}

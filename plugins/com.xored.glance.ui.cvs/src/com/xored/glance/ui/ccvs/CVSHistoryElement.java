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

import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;

@SuppressWarnings("restriction")
public class CVSHistoryElement implements ICVSHistoryNode {

	private final String elementName;
	private final int type;
	private String date;
	private final Map<String, ICVSHistoryNode> children;
	private final ICVSHistoryNode parent;
	private String revision;

	private int count;

	public CVSHistoryElement(String elementName, int type,
			ICVSHistoryNode parent, String revision) {
		this.parent = parent;
		this.count = 1;
		this.elementName = elementName;
		this.type = type;
		this.revision = revision;
		this.children = new HashMap<String, ICVSHistoryNode>();
	}

	public String getRevision() {
		return this.revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public ICVSRepositoryLocation getRepository() {
		return parent.getRepository();
	}

	public ICVSHistoryNode getParent() {
		return this.parent;
	}

	public ICVSHistoryNode[] getNodeChildren() {
		ICVSHistoryNode[] res = children.values().toArray(
				new ICVSHistoryNode[0]);
		Arrays.sort(res, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((ICVSHistoryNode) arg0).getElementName().compareTo(
						((ICVSHistoryNode) arg1).getElementName());
			}
		});
		return res;
		// return children.values().toArray(new ICVSHistoryNode[0]);
	}

	public String getElementName() {
		return this.elementName;
	}

	public int getType() {
		return this.type;
	}

	public String getDate() {
		return this.date;
	}

	public ICVSHistoryNode getChild(String name) {
		return children.get(name);
	}

	public ICVSHistoryNode createFolder(String fullName) {
		int ind = fullName.indexOf("/");
		String curFolder = fullName;
		String childFolder = null;
		if (ind != -1) {
			curFolder = fullName.substring(0, ind);
			childFolder = fullName.substring(ind + 1);
		}
		ICVSHistoryNode elem = children.get(curFolder);
		if (elem == null) {
			elem = new CVSHistoryElement(curFolder, ICVSHistoryNode.FOLDER,
					this, null);
			children.put(curFolder, elem);
		}
		if (childFolder != null) {
			return elem.createFolder(childFolder);
		}
		return elem;
	}

	public ICVSHistoryNode findFolder(String fullName) {
		int ind = fullName.indexOf("/");
		String curFolder = fullName;
		String childFolder = null;
		if (ind != -1) {
			curFolder = fullName.substring(0, ind);
			childFolder = fullName.substring(ind + 1);
		}
		ICVSHistoryNode elem = children.get(curFolder);
		if (elem != null) {
			if (childFolder == null) {
				return elem;
			}
			return elem.findFolder(childFolder);
		}
		return null;
	}

	public ICVSHistoryNode findFile(String fileName) {
		ICVSHistoryNode elem = children.get(fileName);
		if (elem != null && elem.getType() == ICVSHistoryNode.FILE
				&& elem.getCount() == 1) {
			return elem;
		}
		return null;
	}

	public ICVSHistoryNode findPhantomFile(String fileName) {
		ICVSHistoryNode elem = children.get(fileName);
		if (elem != null && elem.getType() == ICVSHistoryNode.FILE) {
			return elem;
		}
		return null;
	}

	public void createFile(String fileName, String revision) {
		ICVSHistoryNode elem = children.get(fileName);
		if (elem == null)
			children.put(fileName, new CVSHistoryElement(fileName,
					ICVSHistoryNode.FILE, this, revision));
		else {
			children.get(fileName).added();
		}
	}

	public void added() {
		++this.count;
	}

	public void removed() {
		--this.count;
	}

	public int getCount() {
		return this.count;
	}

	public void removeFile(String name) {
		ICVSHistoryNode elem = children.get(name);
		if (elem != null && elem.getType() == ICVSHistoryNode.FILE) {
			// children.remove(name);
			elem.removed();
		}
	}

	public String getFullPath() {
		if (parent != null) {
			return parent.getFullPath() + '/' + this.elementName;
		}
		return this.elementName;
	}

	public List<ICVSHistoryNode> getAllElements() {
		Collection<ICVSHistoryNode> elems = children.values();
		List<ICVSHistoryNode> fullSubtree = new ArrayList<ICVSHistoryNode>();
		// TODO do this cycle need?
		for (ICVSHistoryNode elem : elems) {
			if (elem.getCount() == 1) {
			}
		}
		for (ICVSHistoryNode elem : elems) {
			fullSubtree.addAll(elem.getAllElements());
		}
		if (this.count == 1) {
			fullSubtree.add(this);
		}
		return fullSubtree;

	}

	public void accept(IHistoryTreeVisitor visitor) {
		if (!visitor.visit(this)) {
			return;
		}
		Set<String> keys = children.keySet();
		if (keys != null) {
			for (String key : keys) {
				children.get(key).accept(visitor);
			}
		}
	}

	public String getText() {
		return this.elementName;
	}

	public void updateRevision(String revision) {
		String rev1 = getRevision();
		if (VersionUtils.compare(revision, rev1) > 0) {
			setRevision(revision);
		}
	}
}

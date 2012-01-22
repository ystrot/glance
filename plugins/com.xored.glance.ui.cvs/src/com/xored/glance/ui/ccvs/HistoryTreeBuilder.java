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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;

@SuppressWarnings("restriction")
public class HistoryTreeBuilder {
	private ICVSRepositoryLocation location;
	private RemoteFolderTree tree;
	private CVSTag tag;

	public HistoryTreeBuilder(ICVSRepositoryLocation location, CVSTag tag) {
		this.tag = tag;
		this.location = location;
		reset();
	}

	public RemoteFolderTree getTree() {
		return tree;
	}

	/**
	 * Reset the builder to prepare for a new build
	 */
	public void reset() {
		tree = new RemoteFolderTree(null, location,
				ICVSRemoteFolder.REPOSITORY_ROOT_FOLDER_NAME, tag);
		tree.setChildren(new ICVSRemoteResource[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.internal.ccvs.core.client.listeners.RDiffSummaryListener
	 * .IFileDiffListener#newFile(java.lang.String, java.lang.String)
	 */
	public void newFile(IPath remoteFilePath, ICVSRemoteFile remoteFile) {
		try {
			addFile(tree, tag, remoteFile, remoteFilePath);
		} catch (CVSException e) {
			e.printStackTrace();
		}
	}

	private void addFile(RemoteFolderTree tree, CVSTag tag,
			ICVSRemoteFile file, IPath filePath) throws CVSException {
		RemoteFolderTree parent = (RemoteFolderTree) getFolder(tree, tag,
				filePath.removeLastSegments(1), Path.EMPTY);
		addChild(parent, file);
	}

	private void addChild(RemoteFolderTree tree, ICVSRemoteResource resource) {
		ICVSRemoteResource[] children = tree.getChildren();
		ICVSRemoteResource[] newChildren;
		if (children == null) {
			newChildren = new ICVSRemoteResource[] { resource };
		} else {
			newChildren = new ICVSRemoteResource[children.length + 1];
			System.arraycopy(children, 0, newChildren, 0, children.length);
			newChildren[children.length] = resource;
		}
		tree.setChildren(newChildren);
	}

	/*
	 * Get the folder at the given path in the given tree, creating any missing
	 * folders as needed.
	 */
	private ICVSRemoteFolder getFolder(RemoteFolderTree tree, CVSTag tag,
			IPath remoteFolderPath, IPath parentPath) throws CVSException {
		if (remoteFolderPath.segmentCount() == 0)
			return tree;
		String name = remoteFolderPath.segment(0);
		ICVSResource child;
		IPath childPath = parentPath.append(name);
		if (tree.childExists(name)) {
			child = tree.getChild(name);
		} else {
			child = new RemoteFolderTree(tree, tree.getRepository(), childPath
					.toString(), tag);
			((RemoteFolderTree) child).setChildren(new ICVSRemoteResource[0]);
			addChild(tree, (ICVSRemoteResource) child);
		}
		return getFolder((RemoteFolderTree) child, tag, remoteFolderPath
				.removeFirstSegments(1), childPath);
	}
}

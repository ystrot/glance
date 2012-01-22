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

import java.util.List;

import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;

@SuppressWarnings("restriction")
public interface ICVSHistoryNode {

	public static int FOLDER = 1;
	public static int FILE = 2;
	public static int CVSTAG = 3;
	public static int REPOSITORY = 4;

	public String getElementName();

	public int getType();

	public String getRevision();

	public void setRevision(String revision);

	public String getFullPath();

	public ICVSHistoryNode[] getNodeChildren();

	public void accept(IHistoryTreeVisitor visitor);

	public ICVSHistoryNode getParent();

	public ICVSHistoryNode getChild(String name);

	public ICVSRepositoryLocation getRepository();

	public List<ICVSHistoryNode> getAllElements();

	public ICVSHistoryNode findFolder(String fullName);

	public ICVSHistoryNode createFolder(String fullName);

	public ICVSHistoryNode findFile(String fileName);

	public ICVSHistoryNode findPhantomFile(String fileName);

	public void createFile(String fileName, String revision);

	public void removeFile(String name);

	public void added();

	public void removed();

	public int getCount();

	public void updateRevision(String revision);

}

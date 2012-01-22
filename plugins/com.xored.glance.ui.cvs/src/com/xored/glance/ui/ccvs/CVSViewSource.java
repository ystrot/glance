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

import org.eclipse.swt.widgets.Tree;

import com.xored.glance.ui.controls.tree.TreeStructSource;
import com.xored.glance.ui.controls.tree.content.TreeContent;

public class CVSViewSource extends TreeStructSource {
	public static Tree tree;

	public CVSViewSource(Tree tree) {
		super(tree);
	}

	@Override
	protected TreeContent createContent() {
		if (CVSViewSource.tree != null) {
			return new CVSHistorySourceTree(CVSViewSource.tree);
		}
		return null;
	}

}

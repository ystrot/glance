/*******************************************************************************
 * Copyright (c) 2012 xored software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     xored software, Inc. - initial API and implementation (Yuri Strot)
 ******************************************************************************/
package com.xored.glance.internal.ui.panels;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.xored.glance.internal.ui.search.SearchManager;

public class PrevSearchResultHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		SearchManager.getIntance().findPrevious();
		return null;
	}
}

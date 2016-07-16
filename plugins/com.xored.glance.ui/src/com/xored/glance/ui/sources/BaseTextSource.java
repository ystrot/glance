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
package com.xored.glance.ui.sources;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class BaseTextSource implements ITextSource {

	public void index(IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.done();
		}
	}

	public boolean isIndexRequired() {
		return false;
	}
	
	public void init() {
	}
}

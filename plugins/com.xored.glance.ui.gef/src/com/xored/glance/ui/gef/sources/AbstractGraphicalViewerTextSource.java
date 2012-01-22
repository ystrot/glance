/*******************************************************************************
 * Copyright (c) 2012 xored software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     xored software, Inc. - initial API and implementation (Igor Zapletnev)
 ******************************************************************************/
package com.xored.glance.ui.gef.sources;

import org.eclipse.core.runtime.IProgressMonitor;

import com.xored.glance.ui.sources.ITextSource;
import com.xored.glance.ui.sources.SourceSelection;

public abstract class AbstractGraphicalViewerTextSource implements ITextSource {

    @Override
    public void index(final IProgressMonitor monitor) {
        monitor.done();
    }

    @Override
    public boolean isIndexRequired() {
        return false;
    }

    @Override
    public SourceSelection getSelection() {
        return null;
    }

    boolean disposed = false;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
    }
}

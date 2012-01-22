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
package com.xored.glance.ui.gef.controls;

import java.util.List;

import org.eclipse.draw2d.IFigure;

import com.xored.glance.ui.sources.ITextBlock;
import com.xored.glance.ui.sources.ITextSourceListener;
import com.xored.glance.ui.sources.Match;

public interface IFigureDecorator {

    void connect(final IFigure figure);

    void disconnect();

    List<ITextBlock> getTextBlocks();

    void showMatches(Match[] matches);

    void selectMatch(Match match);

    void addTextSourceListener(ITextSourceListener listener);

    void removeTextSourceListener(ITextSourceListener listener);
}

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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.draw2d.AncestorListener;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.text.TextFlow;

import com.xored.glance.ui.gef.blocks.FigureTextBlock;
import com.xored.glance.ui.gef.blocks.LabelTextBlock;
import com.xored.glance.ui.gef.blocks.TextFlowBlock;
import com.xored.glance.ui.sources.ITextBlock;
import com.xored.glance.ui.sources.ITextSourceListener;
import com.xored.glance.ui.sources.Match;

public class FigureDecorator implements IFigureDecorator {

    private final ListenerList listeners = new ListenerList();

    private final List<ITextBlock> cachedBlocks = new ArrayList<ITextBlock>();

    private final LayoutListener layoutListener = new LayoutListener.Stub() {

        @Override
        public void invalidate(final IFigure container) {
            updateHighlightFigureBounds();
        }

        @Override
        public void postLayout(final IFigure container) {
            updateHighlightFigureBounds();
        }

        @Override
        public boolean layout(final IFigure container) {
            updateHighlightFigureBounds();
            return false;
        }

        @Override
        public void setConstraint(final IFigure child, final Object constraint) {
            updateHighlightFigureBounds();
        }
    };

    private final FigureListener figureListener = new FigureListener() {

        public void figureMoved(final IFigure source) {
            updateHighlightFigureBounds();
        }
    };

    public FigureDecorator(final IFigure figure) {
        connect(figure);
    }

    private static final String LAYER_KEY = "matchHighlight";

    private HighlightLayer highLightLayer;

    public void connect(final IFigure figure) {
        createHighlightLayer(figure);
        collectTextBlocks(figure, cachedBlocks);
    }

    public void disconnect() {
        cachedBlocks.clear();
        highLightLayer.removeAll();
    }

    private void createHighlightLayer(IFigure figure) {
        while (!(figure.getParent() instanceof LayeredPane)) {
            figure = figure.getParent();
        }
        final LayeredPane pane = (LayeredPane) figure.getParent();
        final Layer figuresLayer = (Layer) figure;

        if (pane.getLayer(LAYER_KEY) == null) {
            final Layer layer = new HighlightLayer();
            layer.setOpaque(false);
            pane.addLayerBefore(layer, LAYER_KEY, figuresLayer);
            layer.setLayoutManager(new XYLayout());

            final IFigure layer0 = (IFigure) pane.getChildren().get(0);
            layer0.addLayoutListener(layoutListener);
            layer0.addFigureListener(figureListener);
        }
        highLightLayer = (HighlightLayer) pane.getLayer(LAYER_KEY);
    }

    public boolean isConnected() {
        return highLightLayer != null;
    }

    public void selectMatch(final Match match) {
        if (match != null && match.getBlock() instanceof FigureTextBlock<?>) {
            final FigureTextBlock<?> figureBlock = (FigureTextBlock<?>) match.getBlock();
            highLightLayer.setSelected(figureBlock.getFigure());
        } else {
            highLightLayer.setSelected(null);
        }
    }

    protected void updateHighlightFigureBounds() {
        highLightLayer.updateHighlightFiguresBounds();
    }

    private class BlockAncestorListener extends AncestorListener.Stub {

        private final ITextBlock block;

        public BlockAncestorListener(final ITextBlock textBlock) {
            block = textBlock;
        }

        @Override
        public void ancestorRemoved(final IFigure ancestor) {
            cachedBlocks.remove(block);
            blocksChanged(new ITextBlock[] { block }, new ITextBlock[0]);
            if (isHighLighted()) {
                highLightLayer.removeAll();
            }
        }

        @Override
        public void ancestorAdded(final IFigure ancestor) {
            if (!cachedBlocks.contains(block)) {
                cachedBlocks.add(block);
                blocksChanged(new ITextBlock[0], new ITextBlock[] { block });
            }
        }
    }

    protected void collectTextBlocks(final IFigure figure, final List<ITextBlock> textBlocks) {
        ITextBlock textBlock;
        if (figure instanceof Label) {
            textBlock = new LabelTextBlock((Label) figure);
            ((LabelTextBlock) textBlock).getFigure()
                .addAncestorListener(new BlockAncestorListener(textBlock));
        } else if (figure instanceof TextFlow) {
            textBlock = new TextFlowBlock((TextFlow) figure);
            ((TextFlowBlock) textBlock).getFigure().addAncestorListener(new BlockAncestorListener(textBlock));
        } else {
            textBlock = null;
        }

        if (!figure.isVisible() || !figure.isShowing()) {
            textBlock = null;
        }
        if (textBlock != null) {
            textBlocks.add(textBlock);
        }

        for (final Object o : figure.getChildren()) {
            if (o instanceof IFigure) {
                collectTextBlocks((IFigure) o, textBlocks);
            }
        }
    }

    protected void highlightMatch(final Match match) {
        final ITextBlock block = match.getBlock();
        if (!cachedBlocks.contains(block)) {
            return;
        }
        if (block instanceof LabelTextBlock) {
            final LabelTextBlock lBlock = (LabelTextBlock) block;
            highLightLayer.add(new HighlightLabel(lBlock.getFigure(), match));
        } else if (block instanceof TextFlowBlock) {
            final TextFlowBlock flowBlock = (TextFlowBlock) block;
            highLightLayer.add(new HighlightFlow(flowBlock.getFigure(), match));
        }
    }

    private boolean isHighLighted() {
        return !highLightLayer.getChildren().isEmpty();
    }

    public List<ITextBlock> getTextBlocks() {
        return cachedBlocks;
    }

    public void showMatches(final Match[] matches) {
        if (isHighLighted()) {
            highLightLayer.removeAll();
        }

        if (isConnected()) {
            for (final Match match : matches) {
                highlightMatch(match);
            }
            updateHighlightFigureBounds();

            if (matches.length > 0) {
                selectMatch(matches[0]);
            }
        }
    }

    public void addTextSourceListener(final ITextSourceListener listener) {
        listeners.add(listener);

    }

    public void blocksChanged(final ITextBlock[] removed, final ITextBlock[] added) {
        final Object[] objects = listeners.getListeners();
        for (final Object object : objects) {
            final ITextSourceListener listener = (ITextSourceListener) object;
            listener.blocksChanged(removed, added);
        }
    }

    public void removeTextSourceListener(final ITextSourceListener listener) {
        listeners.remove(listener);
    }
}

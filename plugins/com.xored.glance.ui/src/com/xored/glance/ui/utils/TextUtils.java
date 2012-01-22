/**
 * 
 */
package com.xored.glance.ui.utils;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

import com.xored.glance.ui.sources.ColorManager;
import com.xored.glance.ui.sources.Match;

/**
 * @author Yuri Strot
 * 
 */
public class TextUtils {

	public static StyleRange[] copy(StyleRange[] ranges) {
		StyleRange[] result = new StyleRange[ranges.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = copy(ranges[i]);
		}
		return result;
	}

	public static StyleRange copy(StyleRange range) {
		StyleRange result = new StyleRange(range);
		result.start = range.start;
		result.length = range.length;
		result.fontStyle = range.fontStyle;
		return result;
	}

	public static StyleRange[] getStyles(TextPresentation presentation) {
		StyleRange[] ranges = new StyleRange[presentation
				.getDenumerableRanges()];
		Iterator<?> e = presentation.getAllStyleRangeIterator();
		for (int i = 0; e.hasNext(); i++) {
			ranges[i] = (StyleRange) e.next();
		}
		return ranges;
	}

	public static void applyStyles(TextPresentation presentation,
			Match[] matches, Match selected) {
		Color bgColor = ColorManager.getInstance().getBackgroundColor();
	    Color selectedBgColor = ColorManager.getInstance().getSelectedBackgroundColor();
		IRegion region = presentation.getExtent();
		Match[] regionMatches = getRangeMatches(region.getOffset(), region
				.getLength(), matches);
		StyleRange[] ranges = new StyleRange[regionMatches.length];
		for (int i = 0; i < regionMatches.length; i++) {
			StyleRange range = new StyleRange(regionMatches[i].getOffset(),
			    regionMatches[i].getLength(), null, 
			        regionMatches[i].equals(selected) ? selectedBgColor : bgColor);
			ranges[i] = range;
		}
		presentation.mergeStyleRanges(ranges);
	}

	private static Match[] getRangeMatches(int start, int length,
			Match[] matches) {
		int from = getPosition(start, matches);
		if (from >= matches.length)
			return Match.EMPTY;
		if (from > 0) {
			Match border = matches[from - 1];
			if (border.getLength() + border.getOffset() > start)
				from--;
		}
		int to = getPosition(start + length, matches) - 1;
		if (from <= to) {
			Match[] result = new Match[to - from + 1];
			System.arraycopy(matches, from, result, 0, result.length);
			return result;
		}
		return Match.EMPTY;
	}

	private static int getPosition(int offset, Match[] matches) {
		int index = Arrays.binarySearch(matches, new Match(null, offset, 0));
		if (index >= 0)
			return index;
		return -index - 1;
	}

}

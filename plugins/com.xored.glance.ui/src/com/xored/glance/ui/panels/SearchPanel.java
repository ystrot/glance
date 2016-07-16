/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Yuri Strot)
 *******************************************************************************/
package com.xored.glance.ui.panels;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.xored.glance.internal.ui.GlanceEventDispatcher;
import com.xored.glance.internal.ui.GlancePlugin;
import com.xored.glance.internal.ui.panels.CheckAction;
import com.xored.glance.internal.ui.panels.ImageAnimation;
import com.xored.glance.internal.ui.preferences.IPreferenceConstants;
import com.xored.glance.internal.ui.search.SearchManager;
import com.xored.glance.internal.ui.search.SearchRule;
import com.xored.glance.ui.sources.Match;
import com.xored.glance.ui.utils.SelectionAdapter;
import com.xored.glance.ui.utils.UIUtils;

/**
 * @author Yuri Strot
 * @author Shinji Kashihara
 */
public abstract class SearchPanel implements ISearchPanel,
		IPreferenceConstants, IPropertyChangeListener {

	/**
	 * @param parent
	 * @param style
	 */
	public SearchPanel() {
		rule = new SearchRule("");
		getPreferences().addPropertyChangeListener(this);
	}

	public void setIndexingState(final int state) {
		indexState = state;

		if (updateInfoThread != null) {
			final UpdateInfoThread thread = updateInfoThread;
			thread.requestStop();
			updateInfoThread = null;
		}
		if (state == INDEXING_STATE_IN_PROGRESS) {
			indexPercent = 0;
			try {
				updateInfoThread = new UpdateInfoThread();
				updateInfoThread.start();
			} catch (final CoreException e) {
				GlancePlugin.log(e.getStatus());
			}
		} else {
			UIUtils.asyncExec(toolBar, new Runnable() {
				public void run() {
					updateInfo(null);
				}
			});
		}
	}

	public void updateIndexingPercent(final double percent) {
		indexPercent = percent;
	}

	@Override
	public void updatePanelLayout() {
	}
	
	public void newTask(final String name) {
		this.taskName = name;
		indexPercent = 0;
	}

	private static URL getWaitImageStream() throws CoreException {
		URL url = FileLocator.find(GlancePlugin.getDefault().getBundle(),
				new Path(GlancePlugin.IMG_WAIT), null);
		try {
			url = FileLocator.resolve(url);
			return url;
		} catch (final IOException e) {
			throw new CoreException(GlancePlugin.createStatus(
					"Can't find wait image", e));
		}
	}

	private Color getWaitBGColor() {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final Color[] color = new Color[1];
		display.syncExec(new Runnable() {
			public void run() {
				color[0] = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			}
		});
		return color[0];
	}

	private class UpdateInfoThread extends ImageAnimation {

		public UpdateInfoThread() throws CoreException {
			super(getWaitImageStream(), getWaitBGColor());
		}

		private volatile boolean stop = false;

		@Override
		protected boolean isTerminated() {
			return stop || indexState != INDEXING_STATE_IN_PROGRESS;
		}

		@Override
		protected void updateImage(final Image image) {
			updateInfo(image);
		}

		public void requestStop() {
			stop = true;
		}
	}

	private void updateInfo(final Image image) {
		if (bIndexing == null || bIndexing.isDisposed()) {
			return;
		}

		if (indexState == INDEXING_STATE_DISABLE) {
			bIndexing.setToolTipText("Index component");
			bIndexing.setSelection(false);
			bIndexing.setEnabled(false);
			if (bIndexing.getImage() == null) {
				bIndexing.setImage(GlancePlugin.getImage(GlancePlugin.IMG_START_INDEXING));
			}
		} else if (indexState == INDEXING_STATE_INITIAL) {
			bIndexing.setToolTipText("Index component");
			bIndexing.setSelection(false);
			bIndexing.setImage(GlancePlugin.getImage(GlancePlugin.IMG_START_INDEXING));
			bIndexing.setEnabled(true);
		} else if (indexState == INDEXING_STATE_FINISHED) {
			bIndexing.setToolTipText("Index finished");
			bIndexing.setSelection(false);
			bIndexing.setEnabled(false);
		} else {
			final StringBuilder buffer = new StringBuilder();
			bIndexing.setSelection(true);
			bIndexing.setImage(image);
			if (taskName != null && taskName.length() > 0) {
				buffer.append(taskName);
				buffer.append(": ");
			}
			buffer.append((int) (indexPercent * 100));
			buffer.append("%. Stop indexing.");
			bIndexing.setToolTipText(buffer.toString());
		}
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String property = event.getProperty();
		if (property != null && property.startsWith(SEARCH_PREFIX)) {
			updateRule();
		}
	}

	public void createContent(final Composite parent) {
		final Composite container = createContainer(parent);
		final GridLayout layout = new GridLayout(3, false);
		layout.verticalSpacing = 0;
		layout.marginHeight = 2;
		layout.marginWidth = 0;
		container.setLayout(layout);
		createIcon(container);
		createText(container, SWT.BORDER);
		createToolBar(container);
		initSize(container);
	}

	public Control getControl() {
		return container;
	}

	protected Composite createContainer(final Composite parent) {
		container = new Composite(parent, SWT.NONE);
		return container;
	}

	public void firstFound(final Match match) {
		UIUtils.asyncExec(title, new Runnable() {
			public void run() {
				setBackground(match != null);
			}
		});
	}

	public void allFound(final Match[] matches) {
		result = matches;
		UIUtils.asyncExec(title, new Runnable() {
			public void run() {
				setBackground(result.length > 0);
			}
		});
	}

	public void finished() {
	}

	protected Label createIcon(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setImage(GlancePlugin.getImage(GlancePlugin.IMG_SEARCH));
		return label;
	}

	protected Control createText(final Composite parent, final int style) {
		title = new Combo(parent, style | SWT.DROP_DOWN);
		final String text = rule.getText();
		title.setText(text);
		loadHistory();
		if (text != null && text.length() > 0 && result.length == 0)
			setBackground(false);
		title.addModifyListener(modifyListener);
		title.addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event) {
				GlanceEventDispatcher.INSTANCE.dispatchKeyPressed(event);
			}
		});
		title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		title.setEnabled(titleEnabled);
		return title;
	}

	protected ToolBar createToolBar(final Composite parent) {
		toolBar = new ToolBar(parent, SWT.FLAT);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
				.applyTo(toolBar);
		if (getPreferences().getBoolean(PANEL_DIRECTIONS)) {
			createNextItem(toolBar);
			createPreviousItem(toolBar);
		}
		createIndexing(toolBar);
		createSettingsMenu(toolBar);
		if (getPreferences().getBoolean(PANEL_CLOSE)) {
			createClose(toolBar);
		}
		return toolBar;
	}

	protected ToolItem createNextItem(final ToolBar bar) {
		String label = GlanceEventDispatcher.INSTANCE.createBindingLabel("Next", GlanceEventDispatcher.NEXT_COMMAND);
		bNext = createTool(bar, label, GlancePlugin.IMG_NEXT,
				new SelectionAdapter() {
					@Override
					public void selected(final SelectionEvent e) {
						findNext();
					}
				});
		return bNext;
	}

	protected ToolItem createPreviousItem(final ToolBar bar) {
		String label = GlanceEventDispatcher.INSTANCE.createBindingLabel("Previous", GlanceEventDispatcher.PREV_COMMAND);
		bPrev = createTool(bar, label, GlancePlugin.IMG_PREV,
				new SelectionAdapter() {
					@Override
					public void selected(final SelectionEvent e) {
						findPrevious();
					}
				});
		return bPrev;
	}

	private void createIndexing(final ToolBar bar) {
		bIndexing = new ToolItem(bar, SWT.CHECK);
		bIndexing.setDisabledImage(GlancePlugin
				.getImage(GlancePlugin.IMG_INDEXING_FINISHED));
		bIndexing.addSelectionListener(new SelectionAdapter() {
			@Override
			public void selected(final SelectionEvent e) {
				if (indexState == INDEXING_STATE_INITIAL) {
					SearchManager.getIntance().index();
				} else if (indexState != INDEXING_STATE_FINISHED) {
					fireIndexCanceled();
				}
			}
		});
	}

	private ToolItem createTool(final ToolBar bar, final String tip,
			final String image, final SelectionListener listener) {
		final ToolItem item = new ToolItem(bar, SWT.PUSH);
		item.setToolTipText(tip);
		item.setImage(GlancePlugin.getImage(image));
		item.addSelectionListener(listener);
		return item;
	}

	protected ToolItem createSettingsMenu(final ToolBar bar) {
		final ToolItem settings = new ToolItem(bar, SWT.PUSH);
		settings.setImage(JFaceResources.getImage(PopupDialog.POPUP_IMG_MENU));
		settings.setDisabledImage(JFaceResources
				.getImage(PopupDialog.POPUP_IMG_MENU_DISABLED));
		settings.setToolTipText("Settings"); //$NON-NLS-1$
		settings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void selected(final SelectionEvent e) {
				showSettings();
			}
		});
		return settings;
	}

	protected ToolItem createClose(final ToolBar bar) {
		final ToolItem close = new ToolItem(bar, SWT.PUSH);
		final ImageDescriptor image = PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE);
		if (image != null)
			close.setImage(image.createImage());
		String label = GlanceEventDispatcher.INSTANCE.createBindingLabel("Close", GlanceEventDispatcher.CLOSE_COMMAND);
		close.setToolTipText(label);
		close.addSelectionListener(new SelectionAdapter() {
			@Override
			public void selected(final SelectionEvent e) {
				closePanel();
			}
		});
		return close;
	}

	protected void showSettings() {
		final MenuManager manager = new MenuManager();
		fillMenu(manager);
		final Menu menu = manager.createContextMenu(getControl());

		final Point location = getControl().getDisplay().getCursorLocation();
		menu.setLocation(location);
		menu.setVisible(true);
	}

	protected void fillMenu(final IMenuManager menu) {
		menu.add(new Separator());
		newAction(menu, SEARCH_CASE_SENSITIVE, LABEL_CASE_SENSITIVE, true);
		final boolean regExp = newAction(menu, SEARCH_REGEXP, LABEL_REGEXP, true).isChecked();
		newAction(menu, SEARCH_CAMEL_CASE, LABEL_CAMEL_CASE, !regExp);
		newAction(menu, SEARCH_WORD_PREFIX, LABEL_WORD_PREFIX, !regExp);
		menu.add(new Separator());
		menu.add(new Action("Clear search history") {
			@Override
			public void run() {
				clearHistory();
			}
		});
		menu.add(new Separator());
		menu.add(new Action("Preferences...") {
			@Override
			public void run() {
				PreferencesUtil.createPreferenceDialogOn(container.getShell(),
						PREFERENCE_PAGE_ID, null, null).open();
			}
		});
	}

	private CheckAction newAction(final IMenuManager menu, final String name,
			final String label, final boolean enable) {
		return newAction(menu, name, label, enable, null);
	}

	private CheckAction newAction(final IMenuManager menu, final String name,
			final String label, final boolean enable, final String path) {
		final CheckAction action = new CheckAction(name, label);
		if (path != null) {
			action.setImageDescriptor(GlancePlugin.getImageDescriptor(path));
		}
		action.setEnabled(enable);
		menu.add(action);
		return action;
	}

	protected void textChanged() {
		final String text = title.getText();
		final boolean empty = text.length() == 0;
		if (empty)
			textEmpty();
		if (bNext != null && !bNext.isDisposed())
			bNext.setEnabled(!empty);
		if (bPrev != null && !bPrev.isDisposed())
			bPrev.setEnabled(!empty);
		updateRule();
	}

	protected void textEmpty() {
		setBackground(true);
	}

	protected void updateRule() {
		rule = new SearchRule(title.getText());
		fireRuleChanged(rule);
	}

	/**
	 * @return the rule
	 */
	public SearchRule getRule() {
		return rule;
	}

	public void setFocus(String text) {
		if (isReady()) {
			if (text == null || text.length() == 0)
				text = rule.getText();
			if (text != null && text.length() > 0) {
				title.setText(text);
				title.setSelection(new Point(0, text.length()));
				textChanged();
			}
			title.forceFocus();
		}
	}

	public void setEnabled(final boolean enabled) {
		if (isReady()) {
			title.setEnabled(enabled);
			titleEnabled = enabled;
		}
	}

	private boolean isReady() {
		return title != null && !title.isDisposed();
	}

	public void addPanelListener(final ISearchPanelListener listener) {
		listeners.add(listener);
	}

	public void removePanelListener(final ISearchPanelListener listener) {
		listeners.remove(listener);
	}

	private void fireRuleChanged(final SearchRule rule) {
		historyDirty = true;
		final Object[] objects = listeners.getListeners();
		for (final Object object : objects) {
			final ISearchPanelListener listener = (ISearchPanelListener) object;
			listener.ruleChanged(rule);
		}
	}

	protected void fireIndexCanceled() {
		final Object[] objects = listeners.getListeners();
		for (final Object object : objects) {
			final ISearchPanelListener listener = (ISearchPanelListener) object;
			listener.indexCanceled();
		}
	}

	public void findNext() {
		updateHistory();
		final Object[] objects = listeners.getListeners();
		for (final Object object : objects) {
			final ISearchPanelListener listener = (ISearchPanelListener) object;
			listener.findNext();
		}
	}

	public void findPrevious() {
		updateHistory();
		final Object[] objects = listeners.getListeners();
		for (final Object object : objects) {
			final ISearchPanelListener listener = (ISearchPanelListener) object;
			listener.findPrevious();
		}
	}

	public void clearHistory() {
		if (title != null && !title.isDisposed()) {
			title.removeModifyListener(modifyListener);
			try {
				title.removeAll();
				findHistory.clear();
			} finally {
				title.addModifyListener(modifyListener);
				historyDirty = false;
			}
		}
	}

	protected void fireClose() {
		updateHistory();
		saveHistory();
		final Object[] objects = listeners.getListeners();
		for (final Object object : objects) {
			final ISearchPanelListener listener = (ISearchPanelListener) object;
			listener.close();
		}
		getPreferences().removePropertyChangeListener(this);
		if (updateInfoThread != null) {
			updateInfoThread.requestStop();
		}
	}

	/**
	 * @return the preferedWidth
	 */
	protected int getPreferedWidth() {
		return preferredWidth;
	}

	/**
	 * @return the preferredHeight
	 */
	protected int getPreferredHeight() {
		return preferredHeight;
	}

	private void initSize(final Composite composite) {
		final Point size = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		preferredWidth = size.x;
		preferredHeight = size.y;
		preferredWidth -= title.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		final int widthInChars = getPreferences().getInt(PANEL_TEXT_SIZE);
		preferredWidth += getTextWidth(title, widthInChars) + 15;
	}

	protected int getTextWidth(final Control control, final int width) {
		final GC gc = new GC(control);
		try {
			gc.setFont(title.getFont());
			return gc.getFontMetrics().getAverageCharWidth() * width;
		} finally {
			gc.dispose();
		}
	}

	/**
	 * Updates history.
	 */
	private void updateHistory() {
		if (title != null && !title.isDisposed() && historyDirty) {
			title.removeModifyListener(modifyListener);
			try {
				String findString = title.getText();
				final Point sel = title.getSelection();
				findString = fixItem(findString);
				if (findString != null) {
					findHistory.remove(findString);
					findHistory.add(0, findString);
					title.removeAll();
					for (final String string : findHistory) {
						title.add(string);
					}
					title.setText(findString);
					title.setSelection(sel);
				}
			} finally {
				title.addModifyListener(modifyListener);
				historyDirty = false;
			}
		}
	}

	private void saveHistory() {
		final StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < findHistory.size() && i < 8; i++) {
			final String item = findHistory.get(i);
			if (i > 0) {
				buffer.append("\n");
			}
			buffer.append(item);
		}
		getPreferences().putValue(HISTORY, buffer.toString());
	}

	private void loadHistory() {
		findHistory = new ArrayList<String>();
		final String content = getPreferences().getString(HISTORY);
		final String[] items = content.split("\n");
		for (final String item : items) {
			findHistory.add(item);
			title.add(item);
		}
	}

	private String fixItem(final String item) {
		if (item.length() == 0) {
			return null;
		}
		final int index = item.indexOf("\n");
		if (index == 0) {
			return null;
		} else if (index > 0) {
			return item.substring(0, index);
		} else {
			return item;
		}
	}

	private IPreferenceStore getPreferences() {
		return GlancePlugin.getDefault().getPreferenceStore();
	}

	protected void setBackground(final boolean found) {
		title.setBackground(found ? GOOD_COLOR : BAD_COLOR);
	}

	protected static final Color GOOD_COLOR = Display.getDefault()
			.getSystemColor(SWT.COLOR_WHITE);
	protected static final Color BAD_COLOR = new Color(Display.getDefault(),
			255, 102, 102);

	protected Composite container;
	protected Combo title;
	private boolean titleEnabled = true;

	private final ListenerList<ISearchPanelListener> listeners = new ListenerList<ISearchPanelListener>();
	private final ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(final ModifyEvent e) {
			textChanged();
		}
	};

	private List<String> findHistory;
	private boolean historyDirty = true;
	private ToolItem bNext;
	private ToolItem bPrev;
	private ToolItem bIndexing;
	private ToolBar toolBar;
	private SearchRule rule;
	private Match[] result = Match.EMPTY;

	private double indexState;
	private double indexPercent;
	private String taskName;

	private int preferredHeight;
	private int preferredWidth;

	private UpdateInfoThread updateInfoThread;
}

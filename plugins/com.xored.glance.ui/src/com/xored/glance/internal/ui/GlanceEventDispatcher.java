package com.xored.glance.internal.ui;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard;
import org.eclipse.ui.keys.IBindingService;

import com.xored.glance.internal.ui.search.SearchManager;

/**
 * @author Yuri Strot
 * @author Shinji Kashihara
 */
@SuppressWarnings("restriction")
public class GlanceEventDispatcher {

	public static final String GLANCE_CTX = "com.xored.glance.ui.context";

	public static final String NEXT_COMMAND = "com.xored.glance.ui.nextResult";
	public static final String PREV_COMMAND = "com.xored.glance.ui.prevResult";
	public static final String FOCUS_COMMAND = "com.xored.glance.commands.focus";
	public static final String CLOSE_COMMAND = "com.xored.glance.commands.close";
	public static final String CLEAR_COMMAND = "com.xored.glance.commands.clearHistory";

	public static GlanceEventDispatcher INSTANCE = new GlanceEventDispatcher();

	private final BindingManager bindingManager;

	private GlanceEventDispatcher() {
		bindingManager = ((BindingService) PlatformUI.getWorkbench()
				.getService(IBindingService.class)).getBindingManager();
	}

	public void dispatchKeyPressed(Event event) {
		@SuppressWarnings("unchecked")
		List<Object> potentialKeyStrokes = WorkbenchKeyboard.generatePossibleKeyStrokes(event);
		if (potentialKeyStrokes.isEmpty()) {
			return;
		}
		String commandID = getBindCommand(KeySequence.getInstance((KeyStroke) potentialKeyStrokes.get(0)));
		if (commandID == null) {
			return;
		} else if (FOCUS_COMMAND.equals(commandID)) {
			SearchManager.getIntance().sourceFocus();
		} else if (NEXT_COMMAND.equals(commandID)) {
			SearchManager.getIntance().findNext();
		} else if (PREV_COMMAND.equals(commandID)) {
			SearchManager.getIntance().findPrevious();
		} else if (CLOSE_COMMAND.equals(commandID)) {
			SearchManager.getIntance().close();
		} else if (CLEAR_COMMAND.equals(commandID)) {
			SearchManager.getIntance().clearHistory();
		}
	}

	private String getBindCommand(KeySequence keySequence) {
		@SuppressWarnings("unchecked")
		Map<KeySequence, List<Binding>> map = bindingManager.getActiveBindingsDisregardingContext();
		List<Binding> bindings = map.get(keySequence);
		if (bindings != null) {
			for (Binding binding : bindings) {
				if (GLANCE_CTX.equals(binding.getContextId())) {
					return binding.getParameterizedCommand().getId();
				}
			}
		}
		return null;
	}
	
	public String createBindingLabel(String label, String commandId) {
		@SuppressWarnings("unchecked")
		Collection<Binding> bindings = bindingManager.getActiveBindingsDisregardingContextFlat();
		for (Binding b : bindings) {
			if (b.getParameterizedCommand().getId().equals(commandId)) {
				return label + " (" + b.getTriggerSequence().format() + ")";
			}
		}
		return label;
	}
}

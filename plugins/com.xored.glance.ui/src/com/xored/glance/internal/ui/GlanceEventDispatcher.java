package com.xored.glance.internal.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard;
import org.eclipse.ui.keys.IBindingService;

import com.xored.glance.internal.ui.search.SearchManager;


@SuppressWarnings("restriction")
public class GlanceEventDispatcher {
    
    public static String GLANCE_CTX = "com.xored.glance.ui.context";
    
    public static String OPEN_COMMAND = "com.xored.glance.commands.open";
    public static String NEXT_COMMAND = "com.xored.glance.ui.nextResult";
    public static String PREV_COMMAND = "com.xored.glance.ui.prevResult";
    public static String CLOSE_COMMAND = "com.xored.glance.commands.close";
    
    public static Set<String> COMMANDS = new HashSet<String>();
    static { 
            COMMANDS.add(OPEN_COMMAND);
            COMMANDS.add(NEXT_COMMAND);
            COMMANDS.add(PREV_COMMAND);
            COMMANDS.add(CLOSE_COMMAND); 
    }
    
    public static GlanceEventDispatcher INSTANCE = new GlanceEventDispatcher();
    
    private IBindingService bindingService;
    
    private GlanceEventDispatcher(){
        bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
    }
    
    public void dispatchKeyPressed(Event event){
        List<Object> potentialKeyStrokes = WorkbenchKeyboard.generatePossibleKeyStrokes(event);
        if (potentialKeyStrokes.isEmpty()){
            return;
        }
        
        String commandID = getBindCommand(KeySequence.getInstance((KeyStroke) potentialKeyStrokes.get(0)));
        if (commandID == null){
            return;
        } else if (OPEN_COMMAND.equals(commandID)){
            SearchManager.getIntance().sourceFocus();
        } else if (NEXT_COMMAND.equals(commandID)){
            SearchManager.getIntance().findNext();
        } else if (PREV_COMMAND.equals(commandID)){
            SearchManager.getIntance().findPrevious();
        } else if (CLOSE_COMMAND.equals(commandID)){
            SearchManager.getIntance().close();
        }
    }
        
    
    public String getBindCommand(KeySequence keySequence){
        for (Binding binding : getBindingService().getBindings()){
            if (binding.getParameterizedCommand() == null){
                continue;
            }
            if (COMMANDS.contains(binding.getParameterizedCommand().getId()) && binding.getTriggerSequence().equals(keySequence)){
                return binding.getParameterizedCommand().getId();
            }
        }
        return null;
    }
    
    public IBindingService getBindingService() {
        return bindingService;
    }
}

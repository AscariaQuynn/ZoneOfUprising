/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui.forms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.form.Form;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.core.Element;
import tonegod.gui.core.ElementManager;

/**
 *
 * @author Ascaria Quynn
 */
abstract public class MyForm extends Form {

    private HashMap<String, Element> elems = new HashMap<String, Element>();

    private LinkedList<String> errors = new LinkedList<String>();

    public MyForm(ElementManager screen) {
        super(screen);
    }

    @Override
    public void addFormElement(Element element) {
        addFormElement(element.getUID(), element);
    }

    public void addFormElement(String name, Element element) {
        super.addFormElement(element);
        if(elems.containsKey(name)) {
            throw new IllegalArgumentException("Element with name '" + name + "' already exist.");
        }
        if(element instanceof TextField) {
            elems.put(name, element);
        }
    }

    public void addError(String error) {
        errors.add(error);
    }

    public void submitForm(Button button) {
        HashMap<String, String> values = new HashMap<String, String>();
        for(Map.Entry<String, Element> entry : elems.entrySet()) {
            values.put(entry.getKey(), entry.getValue().getText());
        }
        // Validate form
        errors.clear();
        onValidate(values);
        
        if(errors.isEmpty()) {
            // Form is valid
            onSuccess(values);
        } else {
            // Form is invalid
            StringBuilder sb = new StringBuilder(errors.size() * 64);
            for(String error : errors) {
                sb.append(error);
                sb.append("\n");
            }
            onError(errors, sb.toString());
        }
    }

    public void onValidate(HashMap<String, String> values) {
        // Do nothing by default
    }

    public void onSuccess(HashMap<String, String> values) {
        // Do nothing by default
    };

    /**
     * Returns errors.
     * @param errors as list
     * @param errs as string imploded by \n
     */
    public void onError(LinkedList<String> errors, String errs) {
        // Do nothing by default
    };
}

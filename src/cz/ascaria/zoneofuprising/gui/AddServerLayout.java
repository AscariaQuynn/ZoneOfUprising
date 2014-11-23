/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import cz.ascaria.zoneofuprising.gui.forms.MyForm;
import java.util.HashMap;
import java.util.LinkedList;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.lists.SelectList;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.controls.windows.Window;

/**
 *
 * @author Ascaria Quynn
 */
public class AddServerLayout extends BaseLayout {

    public SelectList selectList;

    private Window win;

    public boolean isOpened() {
        return null != win;
    }

    @Override
    public void open() {
        super.open();
        check();
        if(isOpened()) {
            close();
        }

        // Create window
        win = new Window(screen, new Vector2f(15f, 15f), new Vector2f(320f, 200f));
        win.setWindowIsMovable(false);
        win.setIsResizable(false);
        win.setWindowTitle("Add Server");
        win.showAsModal(false);

        final MyForm form = new MyForm(screen) {

            @Override
            public void onValidate(HashMap<String, String> values) {
                /*if(values.get("name").isEmpty()) {
                    addError("Server Name must be filled.");
                }*/
                if(values.get("host").isEmpty()) {
                    addError("Server Host must be filled.");
                }
            }

            @Override
            public void onSuccess(HashMap<String, String> values) {
                if(null != selectList) {
                    selectList.addListItem(values.get("host"), values.get("host"));
                }
                close();
            }

            @Override
            public void onError(LinkedList<String> errors, String errs) {
                guiManager.showAlert("Form validation", errs);
            }
        };

        Label labelName = new Label(screen, new Vector2f(50f, 50f), new Vector2f(100f, 20f));
        labelName.setText("Server Name:");
        win.addChild(labelName);

        /*TextField inputName = new TextField(screen, new Vector2f(170f, 50f));
        win.addChild(inputName);
        form.addFormElement("name", inputName);
        inputName.setTabFocus();*/

        Label labelHost = new Label(screen, new Vector2f(50f, 80f), new Vector2f(100f, 20f));
        labelHost.setText("Server Host:");
        win.addChild(labelHost);

        TextField inputHost = new TextField(screen, new Vector2f(170f, 80f));
        win.addChild(inputHost);
        form.addFormElement("host", inputHost);

        // Add button
        ButtonAdapter addServer = new ButtonAdapter(screen, new Vector2f(50f, 150f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                form.submitForm(this);
            }
        };
        addServer.setText("Add Host");
        win.addChild(addServer);

        // Storno button
        ButtonAdapter storno = new ButtonAdapter(screen, new Vector2f(170f, 150f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                close();
            }
        };
        storno.setText("Storno");
        win.addChild(storno);


        screen.addElement(win);
        win.centerToParent();
    }

    @Override
    public void close() {
        super.close();
        selectList = null;
        if(null != win) {
            win.hide();
            screen.removeElement(win);
            win = null;
        }
    }
}

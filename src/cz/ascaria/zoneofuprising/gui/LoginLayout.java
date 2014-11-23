/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import cz.ascaria.network.ClientManager;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.gui.forms.MyForm;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.text.Password;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.controls.windows.Window;

/**
 *
 * @author Ascaria Quynn
 */
public class LoginLayout extends BaseLayout {

    private ClientManager clientManager;
    private InputManager inputManager;

    private Panel background;
    private MyForm myForm;

    @Override
    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        super.initialize(stateManager, app);

        clientManager = app.getClientManager();
        inputManager = app.getInputManager();
    }

    public boolean isOpened() {
        return null != background;
    }

    @Override
    public void open() {
        super.open();
        check();
        if(isOpened()) {
            close();
        }

        // TODO: refactor
        inputManager.setMouseCursor(null);
        inputManager.setCursorVisible(true);

        // Create panel
        background = new Panel(screen, Vector2f.ZERO, dimensions, Vector4f.ZERO, "Interface/bg-blue-planet.jpg");
        background.setIsMovable(false);
        background.setIsResizable(false);

        createMyForm();
        createLoginInputs(myForm, background);

        // Create logo
        background.addChild(getLogo());



        screen.addElement(background);
    }

    @Override
    public void close() {
        super.close();
        if(null != background) {
            background.hide();
            screen.removeElement(background);
            background = null;
        }
    }

    private void createMyForm() {
        myForm = new MyForm(screen) {
            @Override
            public void onValidate(HashMap<String, String> values) {
                // Validate if email is valid
                try {
                    String emailReg = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
                    Boolean emailValid = values.get("email").matches(emailReg);
                    if(!emailValid) {
                        addError("E-mail is not in valid format.");
                    }
                } catch (Exception e) {
                    addError(e.getMessage());
                }
                // Validate if password is filled
                if(values.get("password").isEmpty()) {
                    addError("Password is required.");
                }
            }

            @Override
            public void onSuccess(HashMap<String, String> values) {
                try {
                    // TODO: remove
                    app.getCentralClient().setHost(values.get("host"));
                    clientManager.login(values.get("email"), values.get("password"));
                } catch(Exception ex) {
                    guiManager.showAlert("Login Form error", ex.getLocalizedMessage());
                    Main.LOG.log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void onError(LinkedList<String> errors, String errs) {
                guiManager.showAlert("Login Form validation", errs);
            }
        };
    }

    private void createLoginInputs(MyForm form, Panel background) {

        Window win = new Window(screen, Vector2f.ZERO, new Vector2f(350f, 200f));
        win.setWindowIsMovable(false);
        win.setWindowTitle("Login");
        win.setIsResizable(false);

        // User email
        Label labelEmail = new Label(screen, new Vector2f(50f, 40f), new Vector2f(80f, 20f));
        labelEmail.setText("E-mail:");
        win.addWindowContent(labelEmail);

        TextField inputEmail = new TextField(screen, new Vector2f(130f, 40f), new Vector2f(170f, 24f));
        win.addWindowContent(inputEmail);
        form.addFormElement("email", inputEmail);
        inputEmail.setText("jarmil.sv@volny.cz");

        // User password
        Label labelPassword = new Label(screen, new Vector2f(50f, 70f), new Vector2f(80f, 20f));
        labelPassword.setText("Password:");
        win.addWindowContent(labelPassword);

        Password inputPassword = new Password(screen, new Vector2f(130f, 70f), new Vector2f(170f, 24f));
        inputPassword.setMask('*');
        win.addWindowContent(inputPassword);
        form.addFormElement("password", inputPassword);
        inputPassword.setText("12345");

        // Host
        Label labelHost = new Label(screen, new Vector2f(50f, 10f), new Vector2f(80f, 20f));
        labelHost.setText("Host:");
        win.addWindowContent(labelHost);

        TextField inputHost = new TextField(screen, new Vector2f(130f, 10f), new Vector2f(170f, 24f));
        win.addWindowContent(inputHost);
        form.addFormElement("host", inputHost);
        inputHost.setText("127.0.0.1");


        // Login button
        Vector2f loginPos = new Vector2f(win.getWidth() / 2f - 50f - 60f, 115f);
        ButtonAdapter login = new ButtonAdapter(screen, loginPos, new Vector2f(100f, 30f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                myForm.submitForm(this);
            }
        };
        login.setText("Login");
        win.addWindowContent(login);

        // Exit button
        Vector2f exitPos = new Vector2f(win.getWidth() / 2f - 50f + 60f, 115f);
        ButtonAdapter exit = new ButtonAdapter(screen, exitPos, new Vector2f(100f, 30f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                app.stop();
            }
        };
        exit.setText("Exit");
        win.addWindowContent(exit);


        background.addChild(win);
        win.centerToParent();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

import com.jme3.app.state.AppStateManager;
import cz.ascaria.zoneofuprising.utils.ConsoleRedirect;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.utils.DateHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author Ascaria Quynn
 */
public class Console {
    
    final public JFrame frame;
    final public JTextPane chatLog;
    final public JTextField messageField;

    protected ConsoleRedirect redirecter;

    private AppStateManager stateManager;
    private ZoneOfUprising app;

    /**
     * Prints output to the console with time mark.
     * @param append 
     */
    public static void sysprintln(String append) {
        System.out.println(DateHelper.time() + " " + append);
    }

    /**
     * Clears input text from console.
     */
    public void clearInputText() {
        messageField.setText(null);
    }

    /**
     * Restores System.out and System.err
     */
    public void shutdown() {
        redirecter.restore();
        frame.dispose();
    }

    /**
     * Creates console instance.
     */
    public Console() {
        frame = new JFrame();
        frame.setTitle("Zone of Uprising Console");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e){
                messageField.requestFocus();
            }
        });

        JPanel chatContainer = new JPanel();
        chatContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        chatContainer.setLayout(new BorderLayout());
        frame.getContentPane().add(chatContainer, BorderLayout.CENTER);

        chatLog = new JTextPane();
        chatLog.setMargin(new Insets(5, 5, 5, 5));
        chatContainer.add(new JScrollPane(chatLog), BorderLayout.CENTER);

        redirecter = new ConsoleRedirect(chatLog);
        redirecter.redirectOut();
        redirecter.redirectErr(Color.RED, null);

        JPanel commandLine = new JPanel();
        commandLine.setLayout(new BoxLayout(commandLine, BoxLayout.X_AXIS));
        commandLine.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        frame.getContentPane().add(commandLine, "South");

        messageField = new JTextField();
        messageField.setBorder(BorderFactory.createLineBorder(Color.gray));
        commandLine.add(messageField);
        commandLine.add(Box.createHorizontalStrut(5));
        commandLine.add(new JButton(new AbstractAction("send") {
            @Override
            public void actionPerformed(ActionEvent event) {
                messageField.postActionEvent();
            }
        }));

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("nic nebude, poustim Main.app.stop()");
                app.stop();
            }
        });
    }

    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        this.stateManager = stateManager;
        this.app = app;
    }

    /**
     * Adds the specified action listener to receive
     * action events from this textfield.
     *
     * @param listener the action listener to be added
     */
    public void addActionListener(ActionListener listener) {
        messageField.addActionListener(listener);
    }

    /**
     * Displays frame.
     */
    public void show() {
        frame.setVisible(true);
    }

    /**
     * Append string to chat log.
     * @param append 
     */
    public void print(String append) {
        try {
            Document doc = chatLog.getDocument();
            doc.insertString(doc.getLength(), DateHelper.time() + " " + append, null);
            // Set selection to the end so that the scroll panel will scroll down.
            chatLog.select(doc.getLength(), doc.getLength());
        } catch (BadLocationException ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Append string line to chat log.
     * @param append 
     */
    public void println(String append) {
        try {
            Document doc = chatLog.getDocument();
            doc.insertString(doc.getLength(), "\n" + DateHelper.time() + " " + append, null);
            // Set selection to the end so that the scroll panel will scroll down.
            chatLog.select(doc.getLength(), doc.getLength());
        } catch (BadLocationException ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
        }
    }
}

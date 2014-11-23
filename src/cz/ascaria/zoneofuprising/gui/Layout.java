/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import tonegod.gui.core.Screen;

/**
 *
 * @author Ascaria Quynn
 */
public interface Layout {

    public void initialize(AppStateManager stateManager, ZoneOfUprising app);

    public void setGuiManager(GuiManager gui);

    public void setScreen(Screen screen);

    public boolean isOpened();

    public void open();

    public void close();
}

package com.vicious.lifelosscore.client.gui;

import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;

public class GUIRegistry {
    public static void onRegister(RegisterGuiOverlaysEvent event){
        event.registerAboveAll("lifeloss.mainoverlay",new LifelossOverlay());
    }
}

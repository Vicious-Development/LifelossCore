package com.vicious.lifelosscore.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicious.lifelosscore.client.widgets.specific.DistanceMeter;
import com.vicious.lifelosscore.client.widgets.specific.HPTextBox;
import com.vicious.lifelosscore.client.widgets.specific.LivesTextBox;
import com.vicious.lifelosscore.client.widgets.specific.TeamTextBox;
import com.vicious.viciouscore.client.gui.widgets.ControlFlag;
import com.vicious.viciouscore.client.gui.widgets.RootWidget;
import com.vicious.viciouscore.client.gui.widgets.glrendered.WidgetRectangularBorder;
import com.vicious.viciouscore.client.gui.widgets.glrendered.WidgetVerticalList;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.awt.*;

public class LifelossOverlay implements IGuiOverlay {
    private static LifelossOverlay instance;
    public static LifelossOverlay getInstance(){
        return instance;
    }
    private final RootWidget root = new RootWidget();
    //private final WidgetRectangle distanceFromCenter = root.addChild(new WidgetRectangle(root,0,0, 20,10,Color.CYAN.getRGB(),0.5F));
    private final WidgetRectangularBorder<?> border = root.addChild(new WidgetRectangularBorder<>(root,2,2,0,0,3)).shadeSelf(Color.BLACK,0f);
    private final WidgetVerticalList<?> list = border.addChild(new WidgetVerticalList<>(root,3,3,0,0));
    private final LivesTextBox<?> lives = list.addChild(new LivesTextBox<>(root,0,0,Color.BLACK,Color.BLACK,0.5f,0.75f));
    private final TeamTextBox<?> team = list.addChild(new TeamTextBox<>(root,0,0,Color.BLACK,Color.BLACK,0.5f,0.75f));
    private final HPTextBox<?> hp = list.addChild(new HPTextBox<>(root,0,0,Color.BLACK,Color.BLACK,0.5f,0.75f));
    public final DistanceMeter<?> distance = list.addChild(new DistanceMeter<>(root,0,0,Color.BLACK,0.5f,0.75f));

    public LifelossOverlay(){
        instance=this;
        lives.setScale(1,1);
        list.listen((v)->regenerate());
        list.addFlags(ControlFlag.SHOULDBROADCASTUPDATES);
    }

    //private final WidgetText distance = distanceFromCenter.addChild(new WidgetText(root,0,0,20,10,"NOT ACTIVE"));
    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        //We don't want mouse interaction in a hud.
        root.render(poseStack,-1,-1,partialTick);
    }
    private boolean isRegening = false;
    private void regenerate(){
        if(isRegening) return;
        isRegening=true;
        border.setHeight(list.getHeight()+6);
        border.setWidth(list.getWidth()+5);
        isRegening=false;
    }
}

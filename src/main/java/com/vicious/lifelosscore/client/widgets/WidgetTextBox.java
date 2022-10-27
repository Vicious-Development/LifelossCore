package com.vicious.lifelosscore.client.widgets;

import com.vicious.viciouscore.client.gui.widgets.*;
import com.vicious.viciouscore.client.gui.widgets.glrendered.WidgetRectangle;
import com.vicious.viciouscore.client.gui.widgets.glrendered.WidgetRectangularBorder;
import com.vicious.viciouscore.client.util.Extents;

import java.awt.*;

public class WidgetTextBox<T extends WidgetTextBox<T>> extends CompoundWidget<T> {
    public WidgetRectangle<?> box;
    public WidgetRectangularBorder<?> border;
    public WidgetText<?> text;
    public WidgetTextBox(RootWidget root, int x, int y, Color bgcol, Color bdcol, float bgopa, float bdopa, Object... bcm) {
        super(root, x, y, 0, 0);
        border = addChild(new WidgetRectangularBorder<>(root,0,0,0,0,2).shadeSelf(bdcol,bdopa));
        box = border.addChild(new WidgetRectangle<>(root,2,2,0,0).shadeSelf(bgcol,bgopa));
        text = box.addChild(new WidgetText<>(root,1,1,0,0,bcm));
        text.listen(this::startRegeneration);
        text.addFlags(ControlFlag.SHOULDBROADCASTUPDATES);
    }

    @Override
    public void regenerate(VCWidget<?> cause){
        border.setHeight(text.getHeight() + 4);
        border.setWidth(text.getWidth() + 2);
        box.setWidth(border.getWidth() - border.getThickness());
        box.setHeight(border.getHeight() - border.getThickness());
        Extents newEx = getDescendantExtents();
        this.setWidth(newEx.getWidth());
        this.setHeight(newEx.getHeight());
    }
}

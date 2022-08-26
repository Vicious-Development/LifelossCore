package com.vicious.lifelosscore.client.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicious.viciouscore.client.gui.widgets.RootWidget;
import com.vicious.viciouscore.client.gui.widgets.VCWidget;
import com.vicious.viciouscore.client.gui.widgets.WidgetRectangle;
import com.vicious.viciouscore.client.gui.widgets.WidgetText;
import com.vicious.viciouscore.client.util.Extents;

public class WidgetTextBox extends VCWidget {
    public WidgetRectangle box;
    public WidgetRectangularBorder border;
    public WidgetText text;
    public WidgetTextBox(RootWidget root, int x, int y, int bgcol, int bdcol, float bgopa, float bdopa, Object... bcm) {
        super(root, x, y, 0, 0);
        border = addChild(new WidgetRectangularBorder(root,0,0,0,0,bdcol,bdopa,2));
        box = border.addChild(new WidgetRectangle(root,2,2,0,0,bgcol,bgopa));
        text = box.addChild(new WidgetText(root,1,1,0,0,bcm));
        text.listen((v)->regenerate());
    }

    @Override
    protected void renderWidget(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void undoGLTransformations(PoseStack stack) {
        super.undoGLTransformations(stack);
    }

    private boolean isRegening = false;
    protected void regenerate(){
        if(isRegening) return;
        isRegening = true;
        border.setHeight(text.getHeight() + 4);
        border.setWidth(text.getWidth() + 2);
        box.setWidth(border.getWidth() - border.getThickness());
        box.setHeight(border.getHeight() - border.getThickness());
        Extents newEx = getDescendantExtents();
        this.setWidth(newEx.getWidth());
        this.setHeight(newEx.getHeight());
        isRegening = false;
    }
}

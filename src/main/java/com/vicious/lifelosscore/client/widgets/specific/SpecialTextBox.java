package com.vicious.lifelosscore.client.widgets.specific;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicious.lifelosscore.client.widgets.WidgetTextBox;
import com.vicious.viciouscore.client.gui.widgets.ControlFlag;
import com.vicious.viciouscore.client.gui.widgets.RenderStage;
import com.vicious.viciouscore.client.gui.widgets.RootWidget;

import java.awt.*;

public class SpecialTextBox<T extends SpecialTextBox<T>> extends WidgetTextBox<T> {
    public SpecialTextBox(RootWidget root, int x, int y, Color bgcol, Color bdcol, float bgopa, float bdopa, Object... bcm) {
        super(root, x, y, bgcol, bdcol, bgopa, bdopa, bcm);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        stack.pushPose();
        this.applyGL(RenderStage.PRE, stack);
        if (visCondition()) {
            stack.pushPose();
            this.applyGL(RenderStage.SELFPRE, stack);
            this.renderWidget(stack, mouseX, mouseY, partialTicks);
            this.applyGL(RenderStage.SELFPOST, stack);
            stack.popPose();
        }
        if(visCondition()) {
            this.forEachChild((c) -> {
                c.render(stack, mouseX, mouseY, partialTicks);
            });
        }
        this.applyGL(RenderStage.POST, stack);
        stack.popPose();
        this.removeFlags(ControlFlag.HOVERED);
    }

    protected boolean visCondition(){
        return isVisible();
    }
}

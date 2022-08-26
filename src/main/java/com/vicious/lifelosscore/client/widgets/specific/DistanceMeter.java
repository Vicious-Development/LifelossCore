package com.vicious.lifelosscore.client.widgets.specific;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicious.lifelosscore.client.gui.ClientData;
import com.vicious.lifelosscore.common.LLFlag;
import com.vicious.viciouscore.client.gui.widgets.RootWidget;
import com.vicious.viciouscore.client.gui.widgets.WidgetRectangle;
import net.minecraft.ChatFormatting;

import java.awt.*;

public class DistanceMeter extends SpecialTextBox {
    protected WidgetRectangle bar;
    public DistanceMeter(RootWidget root, int x, int y, int bdcol, float bgopa, float bdopa) {
        super(root, x, y, Color.WHITE.getRGB(), bdcol, bgopa, bdopa, "DFC: ");
        bar = box.addChild(new WidgetRectangle(root,x,y,0,0,Color.BLACK.getRGB(),bgopa));
    }

    @Override
    protected void renderWidget(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(stack, mouseX, mouseY, partialTicks);
        text.setText(ChatFormatting.BLACK,ChatFormatting.BOLD,"DFC: ", ClientData.distToCenter, "m");
    }

    @Override
    protected void doGLTransformations(PoseStack stack) {
        super.doGLTransformations(stack);
    }

    @Override
    public void regenerate() {
        super.regenerate();
        float unacceptability = ClientData.percentUnnacceptable();
        bar.setHeight(box.getHeight());
        bar.setWidth((int) (unacceptability*box.getWidth()));
        bar.setRGB(getIntFromColor(unacceptability,1.0f-unacceptability,0));
    }
    @Override
    public boolean visCondition() {
        return super.visCondition() && ClientData.hasTeam() && ClientData.flagActive(LLFlag.TEAMSACTIVE);
    }

    //Stackoverflow momento
    public int getIntFromColor(float r, float g, float b){
        int R = Math.round(255 * r);
        int G = Math.round(255 * g);
        int B = Math.round(255 * b);

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }
}

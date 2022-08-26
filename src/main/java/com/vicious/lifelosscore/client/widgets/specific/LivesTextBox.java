package com.vicious.lifelosscore.client.widgets.specific;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicious.lifelosscore.client.LifeColor;
import com.vicious.lifelosscore.client.gui.ClientData;
import com.vicious.lifelosscore.common.LLFlag;
import com.vicious.viciouscore.client.gui.widgets.RootWidget;
import net.minecraft.ChatFormatting;

public class LivesTextBox extends SpecialTextBox {
    public LivesTextBox(RootWidget root, int x, int y, int bgcol, int bdcol, float bgopa, float bdopa) {
        super(root, x, y, bgcol, bdcol, bgopa, bdopa, "");
    }

    @Override
    protected void renderWidget(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(stack, mouseX, mouseY, partialTicks);
        this.text.setText(LifeColor.of(ClientData.lives), ChatFormatting.BOLD,"Lives: " + ClientData.lives);
    }

    @Override
    public boolean visCondition() {
        return super.visCondition() && ClientData.flagActive(LLFlag.LIVESACTIVE);
    }

}

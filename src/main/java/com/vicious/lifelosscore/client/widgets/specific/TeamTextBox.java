package com.vicious.lifelosscore.client.widgets.specific;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicious.lifelosscore.client.gui.ClientData;
import com.vicious.lifelosscore.common.LLFlag;
import com.vicious.viciouscore.client.gui.widgets.RootWidget;
import net.minecraft.ChatFormatting;

import java.awt.*;

public class TeamTextBox<T extends SpecialTextBox<T>> extends SpecialTextBox<T> {
    public TeamTextBox(RootWidget root, int x, int y, Color bgcol, Color bdcol, float bgopa, float bdopa, Object... bcm) {
        super(root, x, y, bgcol, bdcol, bgopa, bdopa, bcm);
    }

    @Override
    protected void renderWidget(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(stack, mouseX, mouseY, partialTicks);
        this.text.setText(ChatFormatting.GREEN, ChatFormatting.BOLD,ClientData.teamName, "'s Center Point: ", ClientData.centerString());
    }


    @Override
    public boolean visCondition() {
        return super.visCondition() && ClientData.flagActive(LLFlag.TEAMSACTIVE) && ClientData.hasTeam();
    }
}


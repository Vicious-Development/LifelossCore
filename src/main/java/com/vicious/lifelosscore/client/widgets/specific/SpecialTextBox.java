package com.vicious.lifelosscore.client.widgets.specific;

import com.vicious.lifelosscore.client.widgets.WidgetTextBox;
import com.vicious.viciouscore.client.gui.widgets.RootWidget;

public class SpecialTextBox extends WidgetTextBox {
    private boolean prevVis = true;
    public SpecialTextBox(RootWidget root, int x, int y, int bgcol, int bdcol, float bgopa, float bdopa, Object... bcm) {
        super(root, x, y, bgcol, bdcol, bgopa, bdopa, bcm);
    }

    @Override
    public boolean isVisible() {
        boolean newVis = visCondition();
        if(newVis != prevVis){
            forEachChild((c)->c.setVisible(newVis));
        }
        prevVis=newVis;
        return prevVis;
    }
    protected boolean visCondition(){
        return visible;
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
    }
}

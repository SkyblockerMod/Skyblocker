package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.rift.RiftProgressWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.rift.GoodToKnowWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.rift.RiftServerInfoWidget;


import net.minecraft.text.Text;

public class RiftScreen extends Screen {

	public RiftScreen(int w, int h, Text footer) {
		super(w, h);
		
		RiftProgressWidget rftProg = new RiftProgressWidget();
		GoodToKnowWidget gtk = new GoodToKnowWidget();
		RiftServerInfoWidget si = new RiftServerInfoWidget();
		
		this.stackWidgetsH(si, gtk);
		this.stackWidgetsH(rftProg);
		this.offCenterL(si);
		this.offCenterL(gtk);
		this.offCenterR(rftProg);
		this.addWidgets(si, gtk, rftProg);
	}

}

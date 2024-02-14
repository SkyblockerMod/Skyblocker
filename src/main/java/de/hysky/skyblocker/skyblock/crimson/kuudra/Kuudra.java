package de.hysky.skyblocker.skyblock.crimson.kuudra;

import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Kuudra {
	public static final String LOCATION = "kuudra";
	static KuudraPhase phase = KuudraPhase.OTHER;

	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(KuudraWaypoints::render);
		ClientLifecycleEvents.CLIENT_STARTED.register(KuudraWaypoints::load);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		ClientReceiveMessageEvents.GAME.register(Kuudra::onMessage);
		Scheduler.INSTANCE.scheduleCyclic(KuudraWaypoints::tick, 20);
	}

	private static void onMessage(Text text, boolean overlay) {
		if (Utils.isInKuudra() && !overlay) {
			String message = Formatting.strip(text.getString());

			if (message.equals("[NPC] Elle: ARGH! All of the supplies fell into the lava! You need to retrieve them quickly!")) {
				phase = KuudraPhase.RETRIEVE_SUPPLIES;
			}

			if (message.equals("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!")) {
				phase = KuudraPhase.DPS;
			}

			if (message.equals("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!")) {
				phase = KuudraPhase.OTHER;
			}
		}
	}

	private static void reset() {
		phase = KuudraPhase.OTHER;
	}

	enum KuudraPhase {
		OTHER,
		RETRIEVE_SUPPLIES,
		DPS;
	}
}

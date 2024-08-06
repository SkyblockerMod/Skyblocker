package de.hysky.skyblocker.utils

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CoroutineUtils {
	var globalJob = CoroutineScope(SupervisorJob() + CoroutineName("Skyblocker"))
	var logger: Logger = LoggerFactory.getLogger("Skyblocker Coroutines")

	@JvmStatic
	fun init() {
		ClientLifecycleEvents.CLIENT_STOPPING.register {
			logger.info("[${logger.name}] Cancelling all coroutines.")
			globalJob.cancel()
		}
	}
}
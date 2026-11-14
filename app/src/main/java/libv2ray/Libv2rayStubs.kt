package libv2ray

interface CoreCallbackHandler {
    fun startup(): Long
    fun shutdown(): Long
    fun onEmitStatus(level: Long, message: String?): Long
}

interface CoreController {
    fun startLoop(config: String, fd: Int): Long
    fun stopLoop(): Long
    val isRunning: Boolean
}

object Libv2ray {
    fun initCoreEnv(path: String, deviceId: String) {
        // No-op stub for runtime environment initialization
    }

    fun newCoreController(handler: CoreCallbackHandler?): CoreController {
        return object : CoreController {
            private var running = false
            override fun startLoop(config: String, fd: Int): Long {
                running = true
                return 0
            }
            override fun stopLoop(): Long {
                running = false
                return 0
            }
            override val isRunning: Boolean
                get() = running
        }
    }

    fun measureOutboundDelay(config: String, url: String): Long {
        return 45L // Mock fast delay for testing/simulation
    }
}

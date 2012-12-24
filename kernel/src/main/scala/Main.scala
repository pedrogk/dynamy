package dynamy

import server._

import java.util.logging._

object Main {
	val logger = Logger.getLogger("dynamy.Main")

	val serverHome = System.getProperty("user.dir")
    System.setProperty("prog.home", serverHome)
	val serverSetup  = new ServerSetup(serverHome)
	

	logger.info("Starting dynamy at %s".format(serverHome))
	serverSetup.initialize()

	val dynamyServer = new DynamyServer(serverHome)

	Runtime.getRuntime().addShutdownHook(new Thread {
		override def run = {
			logger.info("Finished service framework")
			dynamyServer.stop
			dynamyServer.waitForStop
		}
	})
	
	def main(args: Array[String]): Unit = {
        import sun.misc._
        val stopSignal = new java.util.concurrent.CountDownLatch(1)
		val handler = new SignalHandler {
            override def handle(sig: Signal) = {
                stopSignal.countDown
            }
        }
        Signal.handle(new Signal("INT"), handler)
		dynamyServer.start
        stopSignal.await()
	}
}

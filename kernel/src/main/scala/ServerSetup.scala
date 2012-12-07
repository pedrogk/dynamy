package dynamy

import java.nio.file._

class ServerSetup(val serverPath: String) {

  val confPath         = Paths.get(serverPath, "conf")
  val logsPath         = Paths.get(serverPath, "logs")
  val kernelProperties = Paths.get(serverPath, "conf", "kernel.properties")
  val initializedPath  = Paths.get(serverPath, ".initialized")

	def isInitialized = Files.exists(initializedPath)

	def initialize() = if(!isInitialized) {
		Files.createDirectories(confPath)
		Files.createDirectories(logsPath)
		Files.copy(getClass.getResourceAsStream("/kernel.properties"), kernelProperties)
		Files.createFile(initializedPath)
	}
	
}
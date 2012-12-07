package dynamy.cache.xmemcached

import dynamy.cache._

import org.osgi.framework._

import java.nio.file.Paths

class Activator extends BundleActivator {
  override def start(context: BundleContext) = {
    val path = 	Paths.get(System.getProperty("prog.home"), "conf", "memcached.json")
    context.registerService(classOf[DynamyCacheService], new DynamyMemcacheManager(path.toString), null)
  }

  override def stop(context: BundleContext) = {
  }
}

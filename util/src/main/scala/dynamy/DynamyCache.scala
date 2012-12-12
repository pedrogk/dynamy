package dynamy.cache

trait LoaderWrapper extends DynamyCache {
  def loadCurrent[T](l: ClassLoader)(func: () => T): T = {
    val loader = Thread.currentThread.getContextClassLoader
    try {
      Thread.currentThread.setContextClassLoader(l)
      func()
    } finally {
      Thread.currentThread.setContextClassLoader(loader)
    }
  }
  abstract override def get[T](keys: Set[String]): Map[String, T] = loadCurrent(getClass.getClassLoader) { ()=>
    super.get(keys)
  }

  abstract override def get[T](key: String): T = loadCurrent(getClass.getClassLoader) { ()=>
    super.get(key)
  }

  abstract override def get[T](key: String, timeout: Long): T = loadCurrent(getClass.getClassLoader) { ()=>
    super.get(key, timeout)
  }

  abstract override def set[T](key: String, value: T): Unit = loadCurrent(getClass.getClassLoader) { ()=>
    super.set(key, value)
  }

  abstract override def set[T](key: String, exp: Int, value: T): Unit = loadCurrent(getClass.getClassLoader) { ()=>
    super.set(key, exp, value)
  }

  abstract override def set[T](key: String, exp: Int, value: T, timeout: Long): Unit = loadCurrent(getClass.getClassLoader) { ()=>
    super.set(key, exp, value, timeout)
  }


}

trait DynamyCacheService {
  def build(name: String): DynamyCache
}

trait DynamyCache {
  def get[T](keys: Set[String]): Map[String, T]
  def get[T](key: String): T
  def get[T](key: String, timeout: Long): T
  def set[T](key: String, value: T): Unit
  def set[T](key: String, exp: Int, value: T): Unit
  def set[T](key: String, exp: Int, value: T, timeout: Long): Unit

  def remove(key: String): Unit
  def clear(): Unit
  def getName: String

  def shutdown()
}


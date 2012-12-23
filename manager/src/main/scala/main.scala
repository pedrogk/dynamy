package dynamy.manager.web

import ru.circumflex._, core._, web._, freemarker._
import java.util.Date

class MainRouter extends Router {
  val log = new Logger("com.test")

  'currentDate := new Date

  get("/test") = "I'm fine, thanks!"
  get("/") = ftl("index.ftl")

}

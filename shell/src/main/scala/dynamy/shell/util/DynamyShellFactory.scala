package dynamy.shell.runtime.util

import dynamy.shell.runtime._
import scala.collection.JavaConversions._
import org.apache.sshd.common.Factory
import org.apache.sshd.server.Command
import java.io.InputStream
import java.io.OutputStream
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.Environment
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.Executors
import java.io.PrintWriter
import org.slf4j.LoggerFactory
import org.apache.sshd.common.util.OsUtils
import jline.console.ConsoleReader
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import jline.UnixTerminal
import jline.WindowsTerminal
import jline.UnsupportedTerminal
import java.io.FilterOutputStream
import dynamy.shell.runtime.ssh.SshTerminal
import org.apache.shiro.SecurityUtils

class DynamyShellFactory extends Factory[Command] {

  private val logger = LoggerFactory.getLogger(classOf[DynamyShellFactory])

  override def create(): Command = new Command {

    private var in: InputStream = _
    private var out: OutputStream = _
    private var err: OutputStream = _
    private var callback: ExitCallback = _

    private var running: AtomicBoolean = new AtomicBoolean(true)

    private val executor = Executors.newSingleThreadExecutor()

    override def setInputStream(in: InputStream): Unit = {
      this.in = in
    }

    override def setOutputStream(out: OutputStream): Unit = {
      this.out = out
    }

    override def setErrorStream(err: OutputStream): Unit = {
      this.err = err
    }

    override def setExitCallback(callback: ExitCallback): Unit = {
      this.callback = callback
    }

    override def start(env: Environment): Unit = {
      val in = new BufferedInputStream(this.in)
      val out = new BufferedOutputStream(new OCRNLFilterOutputStream(this.out))
      executor.submit(new Runnable() {
        def run() {
          val printWriter = new PrintWriter(out)
          printBanner(printWriter)

          val t = new SshTerminal(env)

          val reader = new ConsoleReader("Dynamy Just-Cloud", in, out, t)

          reader.setPrompt("dynamy> ")

          reader.addCompleter(new DynamyCompleter)

          while (running.get()) {
            try {
              var commandString = reader.readLine().trim()

              if (commandString.length() > 0) {
                val args = commandString.split("\\s+")
                if (args(0).equals("exit")) {
                  running.set(false)
                } else if (args(0).equals("clear")) {
                  reader.drawLine()
                  reader.clearScreen()
                } else if (args(0).equals("help")) {
                  printHelp(printWriter, t)
                } else {
                  commandString = args(0)
                  if (DynamyCommandOsgiHelper.isAlias(commandString)) {
                    commandString = DynamyCommandOsgiHelper.getCommandForAlias(commandString)
                  }
                  if (commandString != null && DynamyCommandOsgiHelper.isCommand(commandString)) {
                    val command = new DynamyCommandHelper(DynamyCommandOsgiHelper.getCommand(commandString))

                    val permittedCollection = for (p <- command.meta.perms()) yield {
                      SecurityUtils.getSubject().isPermitted(p)
                    }

                    val permitted: Boolean =
                      permittedCollection.forall(_ == true) &&
                        SecurityUtils.getSubject().hasAllRoles(command.meta.roles().toSeq)

                    if (permitted) {
                      val data = command.execute(args)
                    } else {
                      printWriter.println("No permission to execute command")
                    }
                  } else {
                    printWriter.println("Unknown command: " + commandString)
                  }
                  printWriter.flush()
                }
              }
            } catch {
              case e => {
                val stack = e.getStackTrace.foldLeft(new StringBuilder()) {
                  (sb, f) =>
                    sb.append("  ")
                    sb.append(f toString)
                    sb += '\n'
                }.toString
                printWriter.println(e.getMessage() + "" + stack)
                printWriter.flush()
              }
            }
          }

          running.set(false)
          callback.onExit(0)
        }
      })

    }

    private def printHelp(out: PrintWriter, terminal: SshTerminal) = {
      out.print("Dynamy Help:")
      out.println()
      val fullWidth = terminal.getWidth()
      val commandWidths = for (c <- DynamyCommandOsgiHelper.getCommands()) yield {
        c.fullName.length()
      }
      val commandWidth = commandWidths.toList.max
      val format = "%-" + commandWidth + "s|%-" + (fullWidth - commandWidth) + "s"
      for (command <- DynamyCommandOsgiHelper.getCommands()) {
        out.println(String.format(format, command.fullName, command.meta.description()))
        out.flush()
        //out.println()
      }
      
    }

    private def printBanner(out: PrintWriter) = {
      val source = io.Source.fromInputStream(getClass().getResourceAsStream("/banner"))
      val lines = source.getLines().mkString("\n")

      out.println("Welcome to Dynamy OSGi Console")
      out.println(lines)

      out.flush()

    }

    override def destroy(): Unit = {
      running.set(false)
    }
  }

  //Read from karaf
  class OCRNLFilterOutputStream(out: OutputStream) extends FilterOutputStream(out) {
    private var lastWasCr: Boolean = _

    override def write(b: Int) = {
      if (!lastWasCr && b.toChar == '\n') {
        out.write('\r')
        out.write('\n')
      } else {
        out.write(b)
      }
      lastWasCr = b.toChar == '\r'
    }

  }

}

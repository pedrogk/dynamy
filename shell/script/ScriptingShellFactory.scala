package com.justcloud.dynamy.shell.runtime.script

import org.osgi.framework.FrameworkUtil
import org.apache.sshd.common.Factory
import org.apache.sshd.server.Command
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream
import org.apache.sshd.server.ExitCallback
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.Executors
import org.apache.sshd.server.Environment
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.PrintWriter
import com.justcloud.dynamy.shell.runtime.ssh.SshTerminal
import jline.console.ConsoleReader
import java.io.FilterOutputStream
import javax.script.ScriptEngineFactory
import javax.script.ScriptEngineManager

class ScriptingShellFactory(language: String) extends Factory[Command] {
  private val logger = LoggerFactory.getLogger(classOf[ScriptingShellFactory])

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
      val engine = new ScriptEngineManager().getEngineByName(language)
      engine.put("bundleContext", FrameworkUtil.getBundle(classOf[ScriptingShellFactory]).getBundleContext())
      executor.submit(new Runnable() {
        def run() {
          val printWriter = new PrintWriter(out)
          printBanner(printWriter)

          val t = new SshTerminal(env)

          val reader = new ConsoleReader("Dynamy Just-Cloud Scripting", in, out, t)

          reader.setPrompt("dynamy script> ")

          while (running.get()) {
            reader.readLine() match {
              case "exit" => running.set(false)
              case "help" => printHelp(printWriter)
              case s: String => {
                try {
                  val retValue = engine.eval(s)
                  printWriter.println(retValue)
                  printWriter.flush()
                } catch {
                  case e => {
                    val stack = e.getStackTrace.foldLeft(new StringBuilder()) {
                      (sb, f) =>
                        sb.append("  ")
                        sb.append(f toString)
                        sb += '\n'
                    }.toString
                    printWriter.println(e.getMessage() +  "" +stack)
                    printWriter.flush()
                  }
                }

              }
            }

          }

          running.set(false)
          callback.onExit(0)
        }
      })

    }

    private def printHelp(out: PrintWriter) = {
      val source = io.Source.fromInputStream(getClass().getResourceAsStream("/help/scripting"))
      val lines = source.getLines().mkString("\n")

      out.println(lines)

      out.flush()

    }

    private def printBanner(out: PrintWriter) = {
      val source = io.Source.fromInputStream(getClass().getResourceAsStream("/banner"))
      val lines = source.getLines().mkString("\n")

      out.println("Welcome to Dynamy Scripting Console")
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

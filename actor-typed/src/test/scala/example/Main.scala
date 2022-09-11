package example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.echopraxia.actor.typed.DefaultAkkaTypedFieldBuilder
import com.tersesystems.echopraxia.plusscala.LoggerFactory

import scala.concurrent.duration._

object Main {

  trait Command
  case object Tick extends Command
  case class Echo(message: String) extends Command

  def main(args: Array[String]): Unit = {
    val system = ActorSystem(MyActor(), "hello")
  }

  object MyActor {
    def apply(): Behavior[Tick.type] = Behaviors.setup { context =>
      Behaviors.withTimers { timers =>
        val echo = context.spawn(EchoActor(), "echo")
        timers.startTimerWithFixedDelay(Tick, 1.seconds)
        Behaviors.receiveMessage {
          case Tick =>
            echo ! Echo(java.time.Instant.now().toString)
            Behaviors.same
        }
      }
    }
  }

  trait MyFieldBuilder extends DefaultAkkaTypedFieldBuilder {
    implicit val commandToValue: ToValue[Command] = cmd => ToValue(cmd.toString)
  }
  object MyFieldBuilder extends MyFieldBuilder

  object EchoActor {
    def apply(): Behavior[Echo] = Behaviors.setup { context =>
      val frozenContext = MyFieldBuilder.keyValue("context" -> context)
      val logger = LoggerFactory.getLogger
        .withFieldBuilder(MyFieldBuilder)
        .withFields(_ => frozenContext) // call-by-name

      Behaviors.receiveMessage { echo =>
        logger.info("echoActor: {}", _.keyValue("echo", echo))
        Behaviors.same
      }
    }
  }
}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.GameManager

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bindActor[GameManager]("game-manager")
  }
}
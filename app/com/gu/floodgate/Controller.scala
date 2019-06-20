package play.api.mvc.legacy

import play.api.mvc._

class Controller extends BaseController {

  override protected def controllerComponents: ControllerComponents = {
    Controller.components /* we don't null check here to avoid the cost associated to it - we expect devs to have done the init properly */
  }

}

object Controller {

  private var components: ControllerComponents = null

  /*
    This need to be called when you initialise the app in app compoments
   */
  def init(c: ControllerComponents) = {
    components = c
  }

}

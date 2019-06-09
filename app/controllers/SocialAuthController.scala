package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import models.daos.UsersDAO
import models.services.UserService
import play.api.i18n.{ I18nSupport, Messages }
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import utils.auth.DefaultEnv

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

/**
 * The social auth controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info service implementation.
 * @param socialProviderRegistry The social provider registry.
 * @param ex                     The execution context.
 */
class SocialAuthController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  usersDAO: UsersDAO,
  authInfoRepository: AuthInfoRepository,
  socialProviderRegistry: SocialProviderRegistry
)(
  implicit
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport with Logger {

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String) = Action.async { implicit request: Request[AnyContent] =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            //            insert new use to database, table users
            socialID = profile.loginInfo.providerKey
            email = profile.email match {
              case None => ""
              case Some(s: String) => s
            }
            firstName = profile.firstName match {
              case None => ""
              case Some(s: String) => s
            }
            lastName = profile.lastName match {
              case None => ""
              case Some(s: String) => s
            }
            usr <- storeNewUserData(socialID, email, firstName, lastName)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
            //            result <- silhouette.env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index()))
          } yield {
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            Redirect("http://localhost:3000/socialsignin?token=" + token.toString())
            //            Ok(Json.obj("token" -> token))
            //            result
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        //        Redirect(routes.SignInController.view()).flashing("error" -> Messages("could.not.authenticate"))
        Unauthorized(Json.obj("message" -> Messages("could.not.authenticate")))
    }
  }

  def storeNewUserData(socialID: String, email: String, firstName: String, lastName: String): Future[String] = {
    var existingUserSocialID = Seq[String]()
    val existingUsers = usersDAO.getBySocialID(socialID)
    existingUsers.map { existingUsers =>
      for (user <- existingUsers) {
        existingUserSocialID = existingUserSocialID :+ user.userSocialID.get
      }
    }
    Await.ready(existingUsers, Duration.Inf)
    if (!existingUserSocialID.contains(socialID)) {
      usersDAO.create(Some(socialID), email, firstName, lastName, None, None, false)
    }
    Future(socialID)
  }
}

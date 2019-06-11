package controllers

import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import models.User
import models.daos.UsersDAO
import models.services.{ AuthTokenService, UserService }
import org.webjars.play.WebJarsUtil
import play.api.i18n.{ I18nSupport, Messages }
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import utils.auth.DefaultEnv

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

/**
 * The `Sign Up` controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param authTokenService       The auth token service implementation.
 * @param avatarService          The avatar service implementation.
 * @param passwordHasherRegistry The password hasher registry.
 * @param mailerClient           The mailer client.
 * @param webJarsUtil            The webjar util.
 * @param assets                 The Play assets finder.
 * @param ex                     The execution context.
 */
class SignUpController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  usersDAO: UsersDAO,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  passwordHasherRegistry: PasswordHasherRegistry,
  mailerClient: MailerClient
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  /**
   * Views the `Sign Up` page.
   *
   * @return The result to display.
   */
  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUpForm.Data].map { data =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
      userService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          Future.successful(BadRequest(Json.obj("message" -> Messages("user.exists"))))
        case None =>
          val authInfo = passwordHasherRegistry.current.hash(data.password)
          val user = User(
            userID = UUID.randomUUID(),
            loginInfo = loginInfo,
            firstName = Some(data.firstName),
            lastName = Some(data.lastName),
            fullName = Some(data.firstName + " " + data.lastName),
            address = Some(data.address),
            email = Some(data.email),
            isAdmin = true,
            avatarURL = None,
            activated = true
          )
          for {
            avatar <- avatarService.retrieveURL(data.email)
            user <- userService.save(user.copy(avatarURL = avatar))

            socialID = loginInfo.providerKey
            email = data.email
            firstName = data.firstName
            lastName = data.lastName
            address = data.address
            usr <- storeNewUserData(socialID, email, firstName, lastName)

            authInfo <- authInfoRepository.add(loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
          } yield {
            silhouette.env.eventBus.publish(SignUpEvent(user, request))
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            Ok(Json.obj("token" -> token))
          }
      }
    }.recoverTotal {
      case error =>
        Future.successful(Unauthorized(Json.obj("message" -> Messages("invalid.data"))))
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
      usersDAO.create(Some(socialID), email, firstName, lastName, None, None, true)
    }
    Future(socialID)
  }
}

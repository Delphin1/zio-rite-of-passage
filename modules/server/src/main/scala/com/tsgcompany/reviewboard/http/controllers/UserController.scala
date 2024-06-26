package com.tsgcompany.reviewboard.http.controllers

import com.tsgcompany.reviewboard.domain.data.UserId
import zio.*
import sttp.tapir.server.*
import com.tsgcompany.reviewboard.domain.errors.*
import com.tsgcompany.reviewboard.services.{JWTService, UserService}
import sttp.tapir.auth
import sttp.tapir.server.ServerEndpoint
import com.tsgcompany.reviewboard.http.endpoints.UserEndpoints
import com.tsgcompany.reviewboard.http.responses.UserResponse


class UserController private (userService: UserService, jwtService: JWTService) extends BaseController with UserEndpoints {
  val create: ServerEndpoint[Any, Task] =
    createUserEndpoint
      .serverLogic { req =>
      userService
        .registerUser(req.email, req.password)
        .map{user => UserResponse(user.email)}
        .either
  }

  val login: ServerEndpoint[Any, Task] =
    loginEndpoint
      .serverLogic { req =>
         userService
           .generateToken(req.email, req.password)
           .someOrFail(UnauthorizedException("Email or password is incorrect"))
           .either

  }

  //change password - check for JWT
  val updatePassword: ServerEndpoint[Any, Task] =
    updataPasswordEndpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic { userId => req =>
        userService
          .updatePassword(req.email, req.oldPassword, req.newPassword)
          .map(user => UserResponse(user.email))
          .either
      }
  //delete account
  val delete: ServerEndpoint[Any, Task] =
    deleteEndpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic { userId => req =>
        userService.deleteUser(req.email, req.password)
          .map(user => UserResponse(user.email))
          .either
      }

  val forgotPassword: ServerEndpoint[Any, Task] =
    forgotPasswordEndpoint
      .serverLogic{ req =>
        userService.sendPasswordRecoveryOTP(req.email).either
      }

  val recoverPassword: ServerEndpoint[Any, Task] =
    recoverPasswordEndpoint
      .serverLogic{ req =>
        userService.recoverPasswordFromToken(req.email, req.token, req.newPassword)
        .filterOrFail(b => b)(UnauthorizedException("The email/token combination is invalid")).unit.either
      }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    create,
    updatePassword,
    delete,
    login,
    forgotPassword,
    recoverPassword
  )
}

object UserController {
  val makeZIO = for {
    userService <- ZIO.service[UserService]
    jwtService <- ZIO.service[JWTService]
  } yield new UserController(userService, jwtService)
}


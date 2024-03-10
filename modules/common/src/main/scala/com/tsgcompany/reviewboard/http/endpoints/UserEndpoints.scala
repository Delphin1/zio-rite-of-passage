package com.tsgcompany.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.tsgcompany.reviewboard.domain.data.UserToken
import com.tsgcompany.reviewboard.http.requests.*
import com.tsgcompany.reviewboard.http.responses.*

trait UserEndpoints extends BaseEndpoint
{ 
  // POST /users {email, password} -> { email }
  val createUserEndpoint = baseEndpoint
      .tag("Users")
      .name("register")
      .description("Register a user account with username and password")
      .in("users")
      .post
      .in(jsonBody[RegisterUserAccount])
      .out(jsonBody[UserResponse])

    // PUT /users/password { email, oudPassword } -> { email }
    // TODO: should be an authorized endpoint JWRT
    val updataPasswordEndpoint = secureBaseEndpoint
        .tag("Users")
        .name("update password")
        .description("Update user password")
        .in("users" / "password")
        .put
        .in(jsonBody[UpdatePasswordRequest])
        .out(jsonBody[UserResponse])

    // DELETE /users { email, password} -> { email }
    // TODO: should be an authorized endpoint JWRT
    val deleteEndpoint = secureBaseEndpoint
        .tag("Users")
        .name("delete account")
        .description("Delete a user account")
        .in("users")
        .delete
        .in(jsonBody[DeleteAccountRequest])
        .out(jsonBody[UserResponse])

      // POST /users/login { email, password }  -> { email, accessToken, expiration }
      val loginEndpoint = baseEndpoint
          .tag("Users")
          .name("login")
          .description("Login and generate JWT token")
          .in("users" / "login")
          .post
          .in(jsonBody[LoginRequest])
          .out(jsonBody[UserToken])

      // forgot password flow
      // POST /users/forgot {email} - 200 OK
      val forgotPasswordEndpoint =
        baseEndpoint
          .tag("Users")
          .name("forgot password endpoint")
          .description("Trigger email for password recovery")
          .in("users" / "forgot")
          .post
          .in(jsonBody[ForgotPasswordRequest])

      // recover password
      // POST /users/recover { email, token, newPassword }
      val recoverPasswordEndpoint =
        baseEndpoint
          .tag("Users")
          .name("recover password endpoint")
          .description("Set new password based on OTP")
          .in("users" / "recover")
          .post
          .in(jsonBody[RecoverPasswordRequest])

}

package com.tsgcompany.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import com.tsgcompany.reviewboard.domain.data.UserToken
import com.tsgcompany.reviewboard.http.requests.*
import com.tsgcompany.reviewboard.http.responses.UserResponse

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

}

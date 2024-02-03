package com.tsgcompany.reviewboard.domain.data

final case class User (
    id: Long,
    email: String,
    hashedPassword: String
                      ){
  def toUserID = UserId(id, email)
}

final case class UserId(
    id: Long,
    email: String
                       )

package com.tsgcompany.reviewboard.components

// moment.js library

import scala.scalajs.*
import scala.scalajs.js.*
import scala.scalajs.js.annotation.*

@js.native
@JSGlobal
class Moment extends js.Object {
  def format(): String = js.native
  def fromNow(): String = js.native
}

// moment.something(2) => String
// m = moment.unix(7863845) => Moment object
// m.format('')
@js.native
@JSImport("moment", JSImport.Default)
object MomentLib extends js.Object {
  def unix(millis: Long): Moment = js.native

}

// API
object Time {
  def unix2hr(millis: Long) =
    MomentLib.unix(millis / 1000).fromNow()
}

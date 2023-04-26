package com.improving.lib.bloom

trait CanGenerateHashFrom[From] {
  def generateHash(from: From): Long
}

object CanGenerateHashFrom {
  implicit val canGenerateHashFromString: CanGenerateHashFrom[String] =
    (from: String) => MurmurHash3.hashString(from)
}

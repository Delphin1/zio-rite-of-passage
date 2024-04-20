package com.tsgcompany.reviewboard.http.responses

import zio.json.JsonCodec

case class CompletionMessage(
    content: String,
    role: String
                            ) derives JsonCodec

case class Completion(
    index: Int,
    message: CompletionMessage
                     ) derives JsonCodec

case class CompletionResponse (
    id: String,
    created: Long,
    choices: List[Completion]
                              ) derives JsonCodec

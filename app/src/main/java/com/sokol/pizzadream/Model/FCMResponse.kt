package com.sokol.pizzadream.Model

class FCMResponse {
    var multicastId: Long? = 0
    var success: Int = 0
    var failure: Int = 0
    var canonicalIds: Int = 0
    var results: List<FCMResult>? = null
    var messageId: Long = 0
}
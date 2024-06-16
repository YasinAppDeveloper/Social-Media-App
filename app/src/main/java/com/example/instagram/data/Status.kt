package com.prasoon.whatsappclone.models

class Status {
    var imageUrl: String? = null
        get() = field
        set(value) {
            field = value
        }

    var timestamp: Long = 0L
        get() = field
        set(value) {
            field = value
        }

    constructor()

    constructor(imageUrl: String?, timestamp: Long) {
        this.imageUrl = imageUrl
        this.timestamp = timestamp
    }
}

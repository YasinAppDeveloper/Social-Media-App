package com.prasoon.whatsappclone.models

class UserStatus {
    var name: String? = null
        get() = field
        set(value) {
            field = value
        }

    var profileImage: String? = null
        get() = field
        set(value) {
            field = value
        }

    var lastUpdated: Long = 0L
        get() = field
        set(value) {
            field = value
        }

    var statuses: ArrayList<Status> = ArrayList()
        get() = field
        set(value) {
            field = value
        }

    constructor()

    constructor(name: String?, profileImage: String?, lastUpdated: Long, statuses: ArrayList<Status>) {
        this.name = name
        this.profileImage = profileImage
        this.lastUpdated = lastUpdated
        this.statuses = statuses
    }
}

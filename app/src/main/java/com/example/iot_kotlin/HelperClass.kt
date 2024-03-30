package com.example.iot_kotlin

class HelperClass {
    var email: String? = null
    var username: String? = null
    var nickname: String? = null
    var password: String? = null
    var gender: String? = null
    var country: String? = null
    var borndate: String? = null
    constructor(username: String?, nickname: String?, email: String?, password: String?) {
        this.username = username
        this.nickname = nickname
        this.email = email
        this.password = password
    }
    constructor(username: String?, nickname: String?, email: String?, password: String?, gender: String?, country: String?, borndate: String?) {
        this.username = username
        this.nickname = nickname
        this.email = email
        this.password = password
        this.gender = gender
        this.country = country
        this.borndate = borndate
    }
    constructor()
}
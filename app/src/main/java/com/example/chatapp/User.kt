package com.example.chatapp

class User {
    var email: String? = null
    var uid: String? = null

    constructor(){}

    constructor(email: String?, uid: String?){
        this.email = email
        this.uid = uid
    }
}
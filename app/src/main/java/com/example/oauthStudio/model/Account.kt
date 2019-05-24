package com.example.oauthStudio.model

import android.os.Parcel
import android.os.Parcelable

class Account() : Parcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(userName)
        dest.writeString(profilePictureLink)
        dest.writeString(dateOfBirth)
        dest.writeString(email)
    }

    override fun describeContents(): Int {
        return 0
    }

    var userName: String? = ""
    var profilePictureLink: String? = ""
    var dateOfBirth: String? = ""
    var email: String? = ""

    constructor(parcel: Parcel) : this() {
        userName = parcel.readString()
        profilePictureLink = parcel.readString()
        dateOfBirth = parcel.readString()
        email = parcel.readString()
    }

    companion object CREATOR : Parcelable.Creator<Account> {
        override fun createFromParcel(parcel: Parcel): Account {
            return Account(parcel)
        }

        override fun newArray(size: Int): Array<Account?> {
            return arrayOfNulls(size)
        }
    }
}
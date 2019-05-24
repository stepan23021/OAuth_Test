package com.example.oauthStudio

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.oauthStudio.model.Account
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import kotlinx.android.synthetic.main.activity_main_screen.*
import java.io.Serializable

class MainScreen : AppCompatActivity(), View.OnClickListener, Serializable {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var imageLoader: ImageLoader

    override fun onClick(v: View?) {
        when (v) {
            sign_out_btn -> signOut()
            disconnect_btn -> revokeAccess()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        sign_out_btn.setOnClickListener(this)
        disconnect_btn.setOnClickListener(this)

        imageLoader = ImageLoader.getInstance()
        imageLoader.init(ImageLoaderConfiguration.createDefault(applicationContext))

        viewAccount()
    }

    private fun viewAccount() {
        val account = intent.getParcelableExtra("Account") as Account?
        if (account != null) {
            username.text = account.userName
            logged_in_user_email.text = account.email

            if (account.profilePictureLink != "") {
                imageLoader.displayImage(account.profilePictureLink, user_photo_IV)
            } else user_photo_IV.setImageResource(R.drawable.images)
        }
        sign_out_btn.visibility = View.VISIBLE
        disconnect_btn.visibility = View.VISIBLE
    }

    private fun signOut() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this) {
            this.finish()
        }
        LoginManager.getInstance().logOut()
    }

    private fun revokeAccess() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            this.finish()
        }
        LoginManager.getInstance().logOut()
    }

    override fun onDestroy() {
        imageLoader.destroy()
        super.onDestroy()
    }
}

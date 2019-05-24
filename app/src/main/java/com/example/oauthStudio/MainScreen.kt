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

    override fun onCreate(savedInstanceState: Bundle?) {
        mGoogleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        initializeOnClickListeners()

        imageLoader = ImageLoader.getInstance()
        imageLoader.init(ImageLoaderConfiguration.createDefault(applicationContext))

        viewAccount()
    }

    private fun initializeOnClickListeners() {
        sign_out_btn.setOnClickListener(this)
        disconnect_btn.setOnClickListener(this)
    }

    private fun viewAccount() {
        val account = intent.getParcelableExtra("Account") as Account
        logged_in_user_name.text = account.userName
        logged_in_user_email.text = account.email

        if (account.profilePictureLink != "") {
            imageLoader.displayImage(account.profilePictureLink, user_photo_IV)
        } else {
            user_photo_IV.setImageResource(R.drawable.images)
        }
    }

    private fun signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            this.finish()
        }
        LoginManager.getInstance().logOut()
    }

    private fun revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this) {
            this.finish()
        }
        LoginManager.getInstance().logOut()
    }

    override fun onDestroy() {
        imageLoader.destroy()
        super.onDestroy()
    }

    override fun onClick(view: View?) {
        when (view) {
            sign_out_btn -> signOut()
            disconnect_btn -> revokeAccess()
        }
    }
}

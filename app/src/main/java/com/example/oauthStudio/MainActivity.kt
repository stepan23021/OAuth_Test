package com.example.oauthStudio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*


private const val RC_SIGN_IN = 9001
private const val TAG = "SignInActivity"

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    lateinit var image: CircleImageView

    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageLoader = ImageLoader.getInstance()
        this.imageLoader.init(ImageLoaderConfiguration.createDefault(baseContext))

        sign_in_btn.setOnClickListener(this)
        sign_out_btn.setOnClickListener(this)
        disconnect_btn.setOnClickListener(this)

        image = findViewById(R.id.userPhoto)


        val gBtn: SignInButton = sign_in_btn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        gBtn.setSize(SignInButton.SIZE_STANDARD)
        gBtn.setColorScheme(SignInButton.COLOR_LIGHT)

    }

    override fun onStart() {
        super.onStart()

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            updateUI(account)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            username.text = account.displayName
            email.text = account.email

            if (account.photoUrl != null) {
                imageLoader.displayImage(account.photoUrl.toString(), image)
            } else image.setImageResource(R.drawable.images)

            sign_in_btn.visibility = View.GONE
            sign_out_btn.visibility = View.VISIBLE
            disconnect_btn.visibility = View.VISIBLE
        } else {
            username.text = null
            email.text = null
            imageLoader.displayImage(null, image)
            sign_in_btn.visibility = View.VISIBLE
            sign_out_btn.visibility = View.GONE
            disconnect_btn.visibility = View.GONE
        }
    }

    private fun signIn() {
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                updateUI(null)
            }
    }

    private fun revokeAccess() {
        mGoogleSignInClient.revokeAccess()
            .addOnCompleteListener(this) {
                updateUI(null)
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_in_btn -> signIn()
            R.id.sign_out_btn -> signOut()
            R.id.disconnect_btn -> revokeAccess()
        }
    }
}

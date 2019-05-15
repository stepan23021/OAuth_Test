package com.example.oauthStudio

import android.content.Intent
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.internal.service.Common
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import com.bumptech.glide.request.RequestOptions


private const val RC_SIGN_IN = 9001
private const val TAG = "SignInActivity"

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sign_in_btn.setOnClickListener(this)
        sign_out_btn.setOnClickListener(this)
        disconnect_btn.setOnClickListener(this)

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
            val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
            Glide.with(this).load(account.photoUrl).apply(options).into(userPhoto)
            email.text = account.email

            sign_in_btn.visibility = View.GONE
            sign_out_btn.visibility = View.VISIBLE
            disconnect_btn.visibility = View.VISIBLE
        } else {
            username.text = null
            email.text = null
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

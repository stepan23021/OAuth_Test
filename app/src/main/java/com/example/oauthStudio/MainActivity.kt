package com.example.oauthStudio

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL


private const val RC_SIGN_IN = 9001
private const val TAG = "SignInActivity"

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var imageLoader: ImageLoader
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageLoader = ImageLoader.getInstance()
        imageLoader.init(ImageLoaderConfiguration.createDefault(applicationContext))

        callbackManager = CallbackManager.Factory.create()

        google_sign_in.setColorScheme(SignInButton.COLOR_LIGHT)
        google_sign_in.setSize(SignInButton.SIZE_STANDARD)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        initializeClickListeners()
        facebookLogin()
    }

    private fun facebookLogin() {
        facebook_login_button.setPermissions("email", "user_birthday", "public_profile")
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                val token: String? = result?.accessToken?.token
                if (token != null) {
                    val request: GraphRequest =
                        GraphRequest.newMeRequest(
                            result.accessToken
                        ) { jsonObject, _ ->
                            if (jsonObject != null) {
                                getData(jsonObject)
                            }
                        }
                    val bundle = Bundle()
                    bundle.putString("fields", "id,email,birthday,name")
                    request.parameters = bundle
                    request.executeAsync()
                }
            }

            override fun onCancel() {
                //TODO add code if needed
            }

            override fun onError(error: FacebookException?) {
                //TODO add code if needed
            }
        })
    }

    private fun getData(jsonObject: JSONObject) {
        try {
            val account = Account()
            account.profilePictureLink =
                URL("https://graph.facebook.com/" + jsonObject.getString("id") + "/picture?width=250&height=250").toString()
            account.email = jsonObject.getString("email")
            account.userName = jsonObject.getString("name")
            account.dateOfBirth = jsonObject.getString("birthday")
            updateUI(account)
        } catch (e: Exception) {

        }
    }

    private fun initializeClickListeners() {
        google_sign_in.setOnClickListener(this)
        sign_out_btn.setOnClickListener(this)
        disconnect_btn.setOnClickListener(this)
        facebook_login_button.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        facebookLogin()
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(generateRawAccount(account))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun generateRawAccount(account: Any?): Account {
        val rawAccount = Account()
        if (account is GoogleSignInAccount) {
            rawAccount.userName = account.displayName
            rawAccount.email = account.email
            rawAccount.profilePictureLink = account.photoUrl.toString()
        }
        return rawAccount
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val gAccount: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            var account = Account()
            if (gAccount != null) {
                account = generateRawAccount(gAccount)
            }
            updateUI(account)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: Account?) {
        if (account != null) {
            username.text = account.userName
            email.text = account.email

            if (account.profilePictureLink != "") {
                imageLoader.displayImage(account.profilePictureLink, user_photo_IV)
            } else user_photo_IV.setImageResource(R.drawable.images)

            google_sign_in.visibility = View.GONE
            sign_out_btn.visibility = View.VISIBLE
            disconnect_btn.visibility = View.VISIBLE
        } else {
            username.text = null
            email.text = null
            imageLoader.displayImage(null, user_photo_IV)

            google_sign_in.visibility = View.VISIBLE
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
        LoginManager.getInstance().logOut()
    }

    private fun revokeAccess() {
        mGoogleSignInClient.revokeAccess()
            .addOnCompleteListener(this) {
                updateUI(null)
            }
    }

    override fun onClick(v: View?) {
        when (v) {
            google_sign_in -> signIn()
            sign_out_btn -> signOut()
            disconnect_btn -> revokeAccess()
            facebook_login_button -> facebookLogin()
        }
    }
}

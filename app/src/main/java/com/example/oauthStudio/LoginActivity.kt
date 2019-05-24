package com.example.oauthStudio

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.oauthStudio.model.Account
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.io.Serializable
import java.util.*


private const val RC_SIGN_IN = 9001
private const val TAG = "SignInActivity"

class LoginActivity : AppCompatActivity(), View.OnClickListener, Serializable {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        google_sign_in.setColorScheme(SignInButton.COLOR_LIGHT)
        google_sign_in.setSize(SignInButton.SIZE_STANDARD)

        callbackManager = CallbackManager.Factory.create()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        initializeClickListeners()
    }

    private fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                Toast.makeText(applicationContext, "Successfully logged in", Toast.LENGTH_SHORT).show()
                Log.d("Success", "Login")
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
                    bundle.putString("fields", "id, email, birthday, name")
                    request.parameters = bundle
                    request.executeAsync()
                }
            }

            override fun onCancel() {
                Toast.makeText(applicationContext, "Login Cancel", Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(applicationContext, error?.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getData(jsonObject: JSONObject) {
        try {
            val account = Account()
            account.profilePictureLink =
                "https://graph.facebook.com/${jsonObject.getString("id")}/picture?width=250&height=250"
            account.email = jsonObject.getString("email")
            account.userName = jsonObject.getString("name")
            account.dateOfBirth = jsonObject.getString("birthday")
            loginProceed(account)
        } catch (e: Exception) {
            Log.e("Something is wrong", e.toString())
        }
    }

    private fun initializeClickListeners() {
        google_sign_in.setOnClickListener(this)
        facebook_login_button.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            loginProceed(generateRawAccount(account))
        }
        val token: AccessToken = AccessToken.getCurrentAccessToken()
        Toast.makeText(applicationContext, token.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun generateRawAccount(account: Any?): Account? {
        if (account == null) {
            return null
        }
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
            var account: Account? = Account()
            if (gAccount != null) {
                account = generateRawAccount(gAccount)
            }
            loginProceed(account)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun loginProceed(account: Account?) {
        if (account != null) {
            val intent = Intent(applicationContext, MainScreen::class.java)
            intent.putExtra("Account", account)
            startActivity(intent)
        } else {
            email.text = null
            password.text = null
            google_sign_in.visibility = View.VISIBLE
            facebook_login_button.visibility = View.VISIBLE
        }
    }

    private fun signIn() {
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onClick(v: View?) {
        when (v) {
            registerTW_btn -> {
                Toast.makeText(applicationContext, "Register clicked", Toast.LENGTH_SHORT).show()
            }
            google_sign_in -> signIn()
            facebook_login_button -> facebookLogin()
        }
    }
}

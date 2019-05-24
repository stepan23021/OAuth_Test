package com.example.oauthStudio

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.oauthStudio.model.Account
import com.example.oauthStudio.model.User
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
import kotlinx.android.synthetic.main.activity_main_screen.*
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
        initializeClickListeners()

        google_sign_in.setColorScheme(SignInButton.COLOR_LIGHT)
        google_sign_in.setSize(SignInButton.SIZE_STANDARD)

        callbackManager = CallbackManager.Factory.create()

        mGoogleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        )
    }

    private fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                Toast.makeText(applicationContext, "Successfully logged in", Toast.LENGTH_SHORT).show()
                if (result?.accessToken?.token != null) {
                    val request: GraphRequest = GraphRequest.newMeRequest(result.accessToken) { jsonObject, _ ->
                        if (jsonObject != null) {
                            getData(jsonObject)
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id, email, birthday, name")
                    request.parameters = parameters
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
            login(account)
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
        val lastSignedAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (lastSignedAccount != null) {
            login(generateRawAccount(lastSignedAccount))
        }
        Toast.makeText(applicationContext, AccessToken.getCurrentAccessToken().toString(), Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun generateRawAccount(account: Any): Account {
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
            if (gAccount != null) {
                login(generateRawAccount(gAccount))
            }
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun login(account: Account) {
        val intent = Intent(applicationContext, MainScreen::class.java)
        intent.putExtra("Account", account)
        startActivity(intent)
    }

    private fun signIn() {
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onClick(view: View?) {
        when (view) {
            registerTW_btn -> registerNewUser()
            google_sign_in -> signIn()
            facebook_login_button -> facebookLogin()
            normal_login_btn -> simpleLogin()
        }
    }

    private fun simpleLogin() {
        val user = User()
        user.email = email.text.toString()
        user.password = password.text.toString()
        val existingUser = validateUser(user)
        login(existingUser)
    }

    private fun validateUser(user: User): Account {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun registerNewUser() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

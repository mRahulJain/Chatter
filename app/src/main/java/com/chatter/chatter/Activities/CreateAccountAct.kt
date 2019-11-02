package com.chatter.chatter.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.Global.putInt
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.chatter.chatter.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_create_account.*

class CreateAccountAct : AppCompatActivity() {

    val RC_SIGN_IN = 1
    lateinit var mAuth : FirebaseAuth
    lateinit var mGoogleApiClient : GoogleApiClient
    lateinit var mAuthListener : FirebaseAuth.AuthStateListener
    lateinit var callbackManager: CallbackManager

    val KEY_GOOGLE_OPEN = "app_open"
    var googleCount = 0
    var type = ""

//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        updateUI()
//    }

    private fun updateUI() {
        val intent = Intent(this, CreateAccountDetailsAct::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val prefs = getPreferences(Context.MODE_PRIVATE)
        googleCount = prefs.getInt(KEY_GOOGLE_OPEN, 0)

        mAuth = FirebaseAuth.getInstance()
        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()

        reqFacebook.setReadPermissions("email", "public_profile")
        reqFacebook.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("myCHECK", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("myCHECK", "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d("myCHECK", "facebook:onError", error)
                // ...
            }
        })

        reqFacebook.setOnClickListener {
            type = "fb"
        }

        if(googleCount == 0) {
            mAuthListener = FirebaseAuth.AuthStateListener {
                if(mAuth.currentUser != null) {
                    val intent = Intent(this, CreateAccountDetailsAct::class.java)
                    googleCount++
                    prefs.edit {
                        putInt(KEY_GOOGLE_OPEN, googleCount)
                    }
                    startActivity(intent)
                    finish()
                }
            }

            mAuth.addAuthStateListener(mAuthListener)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, GoogleApiClient.OnConnectionFailedListener {
                Toast.makeText(this,
                    "Something's wrong",
                    Toast.LENGTH_SHORT).show()
            })
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        reqGoogle.setOnClickListener {
            type = "google"
            if(googleCount != 0) {
                mAuthListener = FirebaseAuth.AuthStateListener {
                    if(mAuth.currentUser != null) {
                        val intent = Intent(this, CreateAccountDetailsAct::class.java)
                        intent.putExtra("password", "")
                        googleCount++
                        prefs.edit {
                            putInt(KEY_GOOGLE_OPEN, googleCount)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        signIn()
                    }
                }
                mAuth.addAuthStateListener(mAuthListener)
            } else {
                signIn()
            }
        }

        reqPhone.setOnClickListener {
            var intent = Intent(this, PhoneAuthAct::class.java)
            startActivity(intent)
        }

        reqMail.setOnClickListener {
            val intent = Intent(this, EmailLoginAct::class.java)
            startActivity(intent)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(type == "fb") {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        } else if(type == "google") {
            if (requestCode == RC_SIGN_IN) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.d("myCHECK", "Google sign in failed", e)
                    // ...
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d("myCHECK", "firebaseAuthWithGoogle:" + account.id!!)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("myCHECK", "signInWithCredential:success")
                    val user = mAuth.currentUser
//                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("myCHECK", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                }
                // ...
            }
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {
        Log.d("myCHECK", "handleFacebookAccessToken:$accessToken")

        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("myCHECK", "signInWithCredential:success")
                    val user = FirebaseAuth.getInstance().currentUser
                    updateUI()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("myCHECK", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}

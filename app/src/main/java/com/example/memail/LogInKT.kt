package com.example.memail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.memail.databinding.ActivityLogInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInKT : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val TAG = "Google SignIn"
    }

    override fun onResume() {
        super.onResume()

        if (firebaseAuth.currentUser != null) {
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.putExtra("user", firebaseAuth.currentUser)
            startActivity(homeIntent)
        } else {
            binding.GoogleSignIn.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        firebaseAuth = Firebase.auth

        binding.GoogleSignIn.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            googleSignInActivityResultLauncher.launch(intent)
        }
    }

    private val googleSignInActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    Log.d(TAG, "onActivityResult : ${result.data!!.extras}")

                    val accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = accountTask.getResult(ApiException::class.java)
                        Log.d(TAG, "onActivityResult : $account")

                        firebaseAuthWithGoogleAccount(account)
                    } catch (e: ApiException) {
                        Log.w(TAG, "onActivityResult : ${e.message}")
                    }
                } else {
                    Log.w(TAG, "onActivityResult : ${result.data}")
                }
            }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { authRes ->
                    Log.d(TAG, "firebaseAuthWithGoogleAccount : ${authRes.user}")

                    if (authRes.additionalUserInfo!!.isNewUser) {
                        /** Create Account */
                        updateUI(true)

                    } else {
                        /** Logged In Account */
                        updateUI(false)
                    }
                }
                .addOnFailureListener { err ->
                    Log.d(TAG, "firebaseAuthWithGoogleAccount : ${err.message}")
                    Toast.makeText(this, "${err.message}", Toast.LENGTH_SHORT).show()
                }
    }

    private fun updateUI(isCreated: Boolean) {
        val firebaseUser = firebaseAuth.currentUser!!

        Log.d(TAG, "firebaseAuthWithGoogleAccount : UID: ${firebaseUser.uid}")
        Log.d(TAG, "firebaseAuthWithGoogleAccount : Email: ${firebaseUser.email}")

        if (isCreated){
            Log.d(TAG, "firebaseAuthWithGoogleAccount : Account created ${firebaseUser.email}")
            Toast.makeText(this, "Account created ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
        }else {
            Log.d(TAG, "firebaseAuthWithGoogleAccount : Existing user ${firebaseUser.email}")
            Toast.makeText(this, "Existing user ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
        }

        onResume()
    }
}
package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chatapp.databinding.ActivityStartBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class StartActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityStartBinding

    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var usersDb: DatabaseReference

    //constants
    private companion object{
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //configure google signIn
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        //firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //google button
        binding.googleBtn.setOnClickListener{
            Log.d(TAG, "onCreate: beginGoogle SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)

        }

        binding.loginBtn1.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signUpBtn1.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }
    }


    private fun checkUser() {
        val currentUser = firebaseAuth.currentUser
        if(currentUser != null){
            //already logged in
            startActivity(Intent(this@StartActivity, ConversationsActivity::class.java))
            finish()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //Great success
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }
            catch(e:Exception){
                //failed google sign in
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account")

        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult->
                Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")

                val currentUser = firebaseAuth.currentUser
                val uid = currentUser!!.uid
                val email = currentUser!!.email


                Log.d(TAG, "firebaseAuthWithGoogleAccount: Uid: $uid")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $email")
                addUserToDatabase(email,uid)

                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account created...")
                    Toast.makeText(this@StartActivity, "Account created", Toast.LENGTH_SHORT).show()
                }
                else{
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing user")
                    Toast.makeText(this@StartActivity, "Logged In", Toast.LENGTH_SHORT).show()
                    checkUser()
                }

            }
            .addOnFailureListener{ e->
                Log.d(TAG,"firebaseAuthWithGoogleAccount: Log in failed ${e.message}")
                Toast.makeText(this@StartActivity, "Failure: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUserToDatabase(email: String?, uid: String) {
        usersDb = FirebaseDatabase.getInstance().getReference()

        usersDb.child("user").child(uid).setValue(User(email,uid))
    }
}
package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.chatapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding:ActivitySignUpBinding

    //actionBar
    private lateinit var actionBar: ActionBar

    //progressDialog
    private lateinit var progressDialog: ProgressDialog

    //firebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    //firebaseDatabase
    private lateinit var usersDb: DatabaseReference

    private var email = ""
    private var password = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //configure progressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Signing Up...")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        //handle click, begin signup
        binding.signUpBtn.setOnClickListener {
            //validate data
            validateData()
        }

        binding.YesAccount.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateData() {
        //get data
        email = binding.NewEmailEt.text.toString().trim()
        password = binding.NewPasswordEt.text.toString().trim()

        //validate
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //invalid email format
            binding.NewEmailEt.error = "Invalid email format"
        }
        else if (TextUtils.isEmpty(password)){
            binding.NewPasswordEt.error = "Please enter password"
        }
        else if(password.length < 8){
            binding.NewPasswordEt.error = "Password must be 8 chars or longer"
        }
        else if (password.contains(" ")){
            binding.NewPasswordEt.error = "Password mustn't contain spaces"
        }
        else{
            //data is valid
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp() {
        //show progress
        progressDialog.show()

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val currentUser = firebaseAuth.currentUser
                var email = currentUser!!.email
                Toast.makeText(this, "Account created with email $email", Toast.LENGTH_SHORT).show()
                addUserToDatabase(email,firebaseAuth.currentUser?.uid!!)
                startActivity(Intent(this, ConversationsActivity::class.java))
                finish()

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "SignUp Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUserToDatabase(email: String?, uid: String) {
        usersDb = FirebaseDatabase.getInstance().getReference()

        usersDb.child("user").child(uid).setValue(User(email,uid))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
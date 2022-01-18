package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ConversationsActivity : AppCompatActivity() {

    private lateinit var userRV: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var usersDb: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        firebaseAuth = FirebaseAuth.getInstance()
        usersDb = FirebaseDatabase.getInstance().getReference()

        userList = ArrayList()
        adapter = UserAdapter(this,userList)

        userRV = findViewById(R.id.usersRV)
        userRV.layoutManager = LinearLayoutManager(this)
        userRV.adapter = adapter

        usersDb.child("user").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()
                for(snap in snapshot.children){
                    val currentUser = snap.getValue(User::class.java)

                    if(firebaseAuth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                //placeholder
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_dropdown,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.logout){
            firebaseAuth.signOut()
            val intent = Intent(this@ConversationsActivity,StartActivity::class.java)
            finish()
            startActivity(intent)
            return true
        }
        else if (item.itemId == R.id.profile){
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        else {
            //implement settings activity
        }
        return true
    }
}
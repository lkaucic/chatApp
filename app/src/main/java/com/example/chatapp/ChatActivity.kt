package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRV: RecyclerView
    private lateinit var editMessage: EditText
    private lateinit var sendBtn: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference

    var recieverRoom: String? = null
    var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        val email = intent.getStringExtra("email")

        val recieverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        dbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = recieverUid + senderUid
        recieverRoom = senderUid + recieverUid

        supportActionBar?.title = email

        chatRV = findViewById(R.id.chatRV)
        editMessage = findViewById(R.id.editMessage)
        sendBtn = findViewById(R.id.sendBtn)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this,messageList)

        chatRV.layoutManager = LinearLayoutManager(this)
        chatRV.adapter = messageAdapter


        dbRef.child("chats").child(senderRoom!!).child("messages").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                messageList.clear()
                for(snap in snapshot.children){
                    val message = snap.getValue(Message::class.java)
                    messageList.add(message!!)
                }
                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                    //not important
            }

        })

        //add message to database
        sendBtn.setOnClickListener{
            val message =  editMessage.text.toString()
            val messageObj = Message(message,senderUid)

            dbRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObj).addOnSuccessListener {
                    dbRef.child("chats").child(recieverRoom!!).child("messages").push()
                        .setValue(messageObj)
                }
            editMessage.setText("")
        }


    }
}
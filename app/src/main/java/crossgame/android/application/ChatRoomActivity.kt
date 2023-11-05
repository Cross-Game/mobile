package crossgame.android.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import crossgame.android.application.databinding.ActivityChatRoomBinding
import crossgame.android.domain.models.Message
import crossgame.android.ui.adapters.message.MessageAdapter

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MessageAdapter
    private var listMessage = mutableListOf<Message>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.listofmessagesinroom

        recyclerView.layoutManager = LinearLayoutManager(this)

        db = Firebase.firestore
        adapter = MessageAdapter(listMessage, this)
        recyclerView.adapter = adapter

        retrieveMessages()

        val findViewById = findViewById<View>(R.id.imageButton6)
        findViewById.setOnClickListener {

            sendMessage(findViewById<EditText>(R.id.textMessage).text.toString());

        }


    }

    private fun sendMessage(text: String) {
        val sendMessage = Message(text,1L)

        db.collection("messages")
            .add(sendMessage)
            .addOnSuccessListener {
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

    private fun retrieveMessages() {


        db.collection("messages").addSnapshotListener()
            { result, error ->
                if (error != null) {
                    Log.w("Tag", "Error listening for messages.", error)
                }

                listMessage.clear()
                for (document in result!!) {
                    listMessage.add(Message(document.data["text"] as String,document.data["id"] as Long))
                }
                adapter.notifyDataSetChanged()
            }
        adapter.notifyDataSetChanged()
    }
}
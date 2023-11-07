package crossgame.android.ui.adapters.message

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SimpleAdapter.ViewBinder
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewbinding.ViewBinding
import crossgame.android.application.R
import crossgame.android.application.databinding.MessageForOthersLayoutBinding
import crossgame.android.application.databinding.MyMessageLayoutBinding
import crossgame.android.domain.models.Message

class MessageAdapter(private val messages: MutableList<Message>, private val context: Context) :
    Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindCurrentUserMessage(message: Message) {
            if (binding is MyMessageLayoutBinding) {
                binding.messageText.text = message.text
            }
        }

        fun bindOtherUserMessage(message: Message) {
            if (binding is MessageForOthersLayoutBinding) {
                binding.messageText.text = message.text
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val message = messages[viewType]
        val view = if (message.id == 1L) {
            MyMessageLayoutBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        } else {
            MessageForOthersLayoutBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        if (message.id == 1L) {
            holder.bindCurrentUserMessage(message)
        } else {
            holder.bindOtherUserMessage(message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.id == 1L) {
            1
        } else {
            0
        }
    }
}
package crossgame.android.ui.adapters.message

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewbinding.ViewBinding
import crossgame.android.application.databinding.MessageForOthersLayoutBinding
import crossgame.android.application.databinding.MyMessageLayoutBinding
import crossgame.android.domain.models.messages.MessageInGroup

class MessageAdapter(private val messageInGroups: MutableList<MessageInGroup>, private val context: Context) :
    Adapter<MessageAdapter.ViewHolder>() {

    // todo ajusta referencia do user id

    inner class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindCurrentUserMessage(messageInGroup: MessageInGroup) {
            if (binding is MyMessageLayoutBinding) {
                binding.messageText.text = messageInGroup.text
            } else if(binding is MessageForOthersLayoutBinding){
                binding.messageText.text = messageInGroup.text
            }
        }

        fun bindOtherUserMessage(messageInGroup: MessageInGroup) {
            if (binding is MessageForOthersLayoutBinding) {
                binding.messageText.text = messageInGroup.text
            }else if(binding is MyMessageLayoutBinding){
                binding.messageText.text = messageInGroup.text
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val message = messageInGroups[viewType]
        val view = if (message.uid == 1L) {
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
        return messageInGroups.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageInGroups[position]
        if (message.uid == 1L) {
            holder.bindCurrentUserMessage(message)
        } else {
            holder.bindOtherUserMessage(message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageInGroups[position]
        return if (message.uid == 1L) {
            0
        } else {
            1
        }
    }
}
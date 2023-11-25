package crossgame.android.ui.adapters.usersRoom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import crossgame.android.application.R
import crossgame.android.application.databinding.UserPortraitRoomLayoutBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.user.UserInRoom
import crossgame.android.service.AutenticationUser
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UsersRoomAdapter(private val users: MutableList<UserInRoom>, private val context: Context) :
    Adapter<UsersRoomAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, userInRoom: UserInRoom)
    }

    class ViewHolder(binding: UserPortraitRoomLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val userPortrait = binding.portraitId
        val image = binding.imageView6
        val name = binding.idUserNameOnUserPortraitRoom
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            UserPortraitRoomLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.image.setImageResource(R.drawable.carbon_user_avatar_empty)

        getPhotoUser(user.id, holder)

        holder.name.text = user.name

        holder.userPortrait.setOnClickListener {
            onItemClickListener?.onItemClick(position, user)
        }
    }

    private fun getPhotoUser(userId: Long, holder: ViewHolder) {
//        val rest = Rest.getInstance(requireActivity()) // todo alterar para autenticado
        val rest = Rest.getInstance()
        rest.create(AutenticationUser::class.java).getPhoto(userId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()?.contentLength()?.toInt() != 0) {
                            val inputStream: InputStream = response.body()!!.byteStream()
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

                            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
                            val decodedByte =
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                            Glide.with(context)
                                .load(bitmap)
                                .apply(RequestOptions.circleCropTransform())
                                .into(holder.image)
                        }
                    } else {
                        Log.i("GET", "Ops, imagem incompatível !")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.i("GET", "Falha ao obter a foto de perfil")
                }
            })
    }
}
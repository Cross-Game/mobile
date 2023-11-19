package crossgame.android.ui.adapters.room

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import crossgame.android.application.R
import crossgame.android.application.databinding.ItemCardUsersInRoomImageUserBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.user.User
import crossgame.android.service.AutenticationUser
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UsersInRoomAdapter(
    private val users: MutableList<User>,
    private val context: Context,
) : Adapter<UsersInRoomAdapter.ViewHolder>() {

    class ViewHolder(binding: ItemCardUsersInRoomImageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val image = binding.imageView5
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemCardUsersInRoomImageUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return UsersInRoomAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position];

        holder.image.setImageResource(R.drawable.group_997)

        getPhotoUser(user.id, holder)
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
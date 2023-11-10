package crossgame.android.domain.models.friends

import android.graphics.Bitmap

data class Friends(
    val friendUserId: Long,
    val username: String,
    //val userMessage: String,
    var friendPhoto: Bitmap?
) {

}


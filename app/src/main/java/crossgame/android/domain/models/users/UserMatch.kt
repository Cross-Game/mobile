package crossgame.android.domain.models.users

import crossgame.android.domain.models.feedbacks.MediaFeedback
import crossgame.android.domain.models.games.GameResponse

data class UserMatch (
    val baseUser : BaseUser,
    val games : List<GameResponse>,
    val img : String,
    val feedback : MediaFeedback,
    val preference : List<String>,
    val platforms : List<String>,
    val qtdFriends : Int
)
package crossgame.android.domain.models.users

import crossgame.android.domain.models.games.GameResponse

data class UserMatch (
    val baseUser : BaseUser,
    val games : List<GameResponse>,
    val img : String
)
package crossgame.android.domain.models.games


data class GameRequestPost(
    val isFavoriteGame: Boolean = true,
    val userNickname: String = "a",
    val gamerId: String = "a",
    val skillLevel: String = "MEDIUM",
    val gameFunction: String = "TOP",
    var GenericGamersIds: List<Int>
)
package crossgame.android.domain.models.games

data class GameResponse(
    val id: Long,
    val platformsType: List<String>,
    val imageGame: ImageGame,
    val gameGenres: List<String>,
    val name: String,
    val platforms: List<String>,
    val cover: Long,
    val genres: List<Int>
)

data class ImageGame(
    val id: Long?,
    val typeImage: String,
    val link: String,
    val image_id: String
)
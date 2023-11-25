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
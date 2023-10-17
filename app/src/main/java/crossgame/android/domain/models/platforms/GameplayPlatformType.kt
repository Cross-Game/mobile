package crossgame.android.domain.models.platforms

enum class GameplayPlatformType(vararg ids: Int) {
    PC(6),
    PLAYSTATION(48),
    XBOX(49),
    MOBILE(34, 39);

    private val ids: IntArray = ids

    companion object {
        fun mapIdsToPlatforms(platformIds: List<Int>): List<GameplayPlatformType> {
            val platformIdMap = HashMap<Int, GameplayPlatformType>()
            for (platformType in values()) {
                for (id in platformType.ids) {
                    platformIdMap[id] = platformType
                }
            }

            val platforms = ArrayList<GameplayPlatformType>()
            var hasMobile = false // Flag para rastrear a presença de "MOBILE"

            for (id in platformIds) {
                if (id == 34 || id == 39) {
                    // Se for 34 ou 39 (MOBILE), verifique se já foi adicionado
                    if (!hasMobile) {
                        platforms.add(MOBILE)
                        hasMobile = true // Define a flag para true após a adição
                    }
                } else {
                    val platformType = platformIdMap[id]
                    platformType?.let { platforms.add(it) }
                }
            }

            return platforms
        }
    }
}
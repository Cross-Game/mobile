package crossgame.android.domain.models.users

import crossgame.android.domain.models.preferences.Preference

data class UserPreference(
    val id : Int,
    val username : String,
    val email : String,
    val role : String,
    val isOnline : Boolean,
    val preference: List<Preference>
)
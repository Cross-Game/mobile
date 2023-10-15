package crossgame.android.domain.models.user

data class DiscordUserResponse(
    val id: String,          // ID do usuário no Discord
    val username: String,    // Nome de usuário do Discord
    val discriminator: String, // Número de discriminação do usuário (ex: #1234)
    val avatar: String?,     // URL do avatar do usuário (pode ser nulo)
    val email: String?       // Email do usuário (pode ser nulo)
)


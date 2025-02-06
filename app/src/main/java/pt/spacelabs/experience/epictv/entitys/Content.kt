package pt.spacelabs.experience.epictv.entitys

data class Content (
    val id: String,
    val poster: String,
    val name: String,
    val time: Int,
    val description: String,
    val isActive: Boolean
)
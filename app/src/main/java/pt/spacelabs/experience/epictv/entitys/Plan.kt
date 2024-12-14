package pt.spacelabs.experience.epictv.entitys

data class Plan (
    val id: String,
    val name: String,
    val have1080: Boolean,
    val have4k: Boolean,
    val qtdProfiles: Int,
    val haveDownloads: Boolean,
    val haveWatchShare: Boolean,
    val valueMonthly: Double,
    val valueYearly: Double
)
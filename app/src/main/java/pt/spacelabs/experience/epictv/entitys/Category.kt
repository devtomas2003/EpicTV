package pt.spacelabs.experience.epictv.entities

class Category {
    var id: String = ""
        get() = field
        set(value) {
            field = value
        }

    var name: String = ""
        get() = field
        set(value) {
            field = value
        }

    var contentList: MutableList<Content> = mutableListOf()
        get() = field
        set(value) {
            field = value
        }
}
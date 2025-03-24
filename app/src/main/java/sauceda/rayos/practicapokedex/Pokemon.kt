package sauceda.rayos.practicapokedex

class Pokemon() {
    var number: String = ""
    var name: String = ""
    var image: String = ""

    constructor(number: String, name: String, image: String) : this() {
        this.number = number
        this.name = name
        this.image = image
    }
}
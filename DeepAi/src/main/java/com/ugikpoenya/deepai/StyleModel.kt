package com.ugikpoenya.deepai

import java.io.Serializable

class StyleModel(title: String, image: String, url: String) : Serializable {
    var title: String? = title
    var image: String? = image
    var url: String? = url
}
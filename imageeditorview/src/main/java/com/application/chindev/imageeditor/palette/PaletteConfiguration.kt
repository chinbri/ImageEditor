package com.application.chindev.imageeditor.palette

import com.application.chindev.imageeditor.GravityEnum
import com.application.chindev.imageeditor.OrientationEnum

data class PaletteConfiguration(
    val visible: Boolean = true,
    val gravity: GravityEnum = GravityEnum.TOP_LEFT,
    val margin: Int = 0,
    val backgroundColor: String = "#FFFFFF",
    val orientation: OrientationEnum = OrientationEnum.VERTICAL,
    val highlightColor: String = ""
)
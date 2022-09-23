package org.mp4parser.isoviewer

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val hexview by cssclass()
    }

    init {

        hexview {

            fontFamily = "monospace"
            columnHeader {
                label {
                    alignment = Pos.BOTTOM_LEFT
                }
            }
        }
    }
}
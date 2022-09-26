package org.mp4parser.isoviewer

import javafx.geometry.Pos
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
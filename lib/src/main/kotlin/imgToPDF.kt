/*
 * Copyright (c) 2025  Hendrik Horstmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kurswahlApp.data

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException


@Throws(IOException::class)
fun createAndSaveImagePDF(file: File, img: BufferedImage, border: Int = 80) {
    PDDocument().use { doc ->
        // a valid PDF document requires at least one page
        val page = PDPage(PDRectangle.A4)
        doc.addPage(page)
        val scale = img.width.toFloat()/(img.width + 2*border)
        val width = page.mediaBox.width * scale
        val x = (page.mediaBox.width - width)/2

        val pdImage = LosslessFactory.createFromImage(doc, img)

        PDPageContentStream(doc, page, AppendMode.APPEND, false).use { contentStream ->
            contentStream.drawImage(pdImage, x, x, width, page.mediaBox.height-2*x)
        }
        doc.save(file)
    }
}
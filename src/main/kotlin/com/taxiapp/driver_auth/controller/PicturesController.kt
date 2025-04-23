package com.taxiapp.driver_auth.controller

import com.taxiapp.driver_auth.service.LocalFileStorageServiceImpl
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Profile("local")
@Controller("/pictures")
class PicturesController(
    private val fileStorageService: LocalFileStorageServiceImpl
) {
    @GetMapping("{filename}")
    fun getPicture(path: String): ResponseEntity<Any> {
        val picture = fileStorageService.getFile(path)
        return if (picture.exists()) {
            ResponseEntity.ok()
                .contentType(if (picture.name.endsWith(".png")) MediaType.IMAGE_PNG else MediaType.IMAGE_JPEG)
                .body(picture.readBytes())
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
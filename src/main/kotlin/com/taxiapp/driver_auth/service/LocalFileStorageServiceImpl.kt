package com.taxiapp.driver_auth.service

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Service
@Profile("local")
class LocalFileStorageServiceImpl : FileStorageService {

    private val rootLocation: Path = Paths.get("uploads")

    init {
        Files.createDirectories(rootLocation)
    }

    override fun storeFile(file: MultipartFile): String {
        val filename = UUID.randomUUID().toString() + "-" + file.originalFilename
        val destinationFile = rootLocation.resolve(filename)
        file.transferTo(destinationFile)
        return filename
    }

    override fun getFileUrl(filePath: String): String {
        return "/pictures/${filePath}"
    }

    fun getFile(filename: String): File {
        val filePath = rootLocation.resolve(filename)
        return filePath.toFile()
    }
}
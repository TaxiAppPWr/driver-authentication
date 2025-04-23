package com.taxiapp.driver_auth.service

import org.springframework.web.multipart.MultipartFile

interface FileStorageService {
    fun storeFile(file: MultipartFile): String
    fun getFileUrl(filePath: String): String
}
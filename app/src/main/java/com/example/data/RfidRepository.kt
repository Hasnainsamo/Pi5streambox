package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RfidRepository(private val rfidDao: RfidDao) {

    val allTags: Flow<List<RfidTag>> = rfidDao.getAllTags()
    val allLogs: Flow<List<ActivityLog>> = rfidDao.getAllLogs()

    suspend fun getTag(uid: String): RfidTag? {
        return rfidDao.getTagByUid(uid)
    }

    suspend fun insertTag(tag: RfidTag) {
        rfidDao.insertTag(tag)
    }

    suspend fun deleteTag(tag: RfidTag) {
        rfidDao.deleteTag(tag)
    }

    suspend fun insertLog(type: String, message: String) {
        rfidDao.insertLog(ActivityLog(type = type, message = message))
    }

    suspend fun clearLogs() {
        rfidDao.clearLogs()
    }

    suspend fun prepopulateIfEmpty() {
        val existingTags = allTags.first()
        if (existingTags.isEmpty()) {
            // Save sample RFID cards/tags
            val defaultTags = listOf(
                RfidTag(
                    uid = "4A:C1:F2:78",
                    title = "Sintel Cinema (4K UHD)",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    isAuthorized = true,
                    streamResolution = "4K UHD",
                    category = "Cinema"
                ),
                RfidTag(
                    uid = "5B:D2:E3:89",
                    title = "Tears of Steel (4K Sci-Fi)",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    isAuthorized = true,
                    streamResolution = "4K UHD",
                    category = "Sci-Fi"
                ),
                RfidTag(
                    uid = "6C:E3:04:9A",
                    title = "Big Buck Bunny Classic UHD",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    isAuthorized = true,
                    streamResolution = "4K UHD",
                    category = "Animation"
                ),
                RfidTag(
                    uid = "7D:F4:15:AB",
                    title = "Subaru Sunset Drive HD",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    isAuthorized = true,
                    streamResolution = "1080p FHD",
                    category = "Promo"
                ),
                RfidTag(
                    uid = "8E:05:26:BC",
                    title = "Restricted Corporate Asset",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                    isAuthorized = false,
                    streamResolution = "4K UHD",
                    category = "Restricted"
                )
            )
            for (tag in defaultTags) {
                rfidDao.insertTag(tag)
            }

            // Insert booting up logs
            insertLog("SUCCESS", "Raspberry Pi 5 Server Boot complete (OS: Bookworm)")
            insertLog("INFO", "4K Video Pipeline initialized on HDMI-0 (UHD @ 60Hz)")
            insertLog("INFO", "RC522 RFID reader loaded at SPI0 (MISO=GPIO9, MOSI=GPIO10)")
            insertLog("INFO", "Access Control policy database loaded successfully")
        }
    }
}

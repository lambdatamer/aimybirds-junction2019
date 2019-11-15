package com.justai.junction.api

import retrofit2.http.GET

interface QuestApi {
    @GET("googlesheet2json?sheet=1&id=1Hw6ohQRIa67kynUFGfl1MhRLsYjd5NmKgXu0NIudDB4")
    suspend fun getQuestList(): List<Quest>
}
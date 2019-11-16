package com.justai.junction.aimybox

import android.content.Context
import com.github.salomonbrys.kotson.put
import com.justai.aimybox.Aimybox
import com.justai.aimybox.api.aimybox.AimyboxDialogApi
import com.justai.aimybox.api.aimybox.AimyboxRequest
import com.justai.aimybox.api.aimybox.AimyboxResponse
import com.justai.aimybox.core.Config
import com.justai.aimybox.core.CustomSkill
import com.justai.aimybox.speechkit.google.platform.GooglePlatformSpeechToText
import com.justai.aimybox.speechkit.google.platform.GooglePlatformTextToSpeech
import java.util.*

fun createAimybox(context: Context): Aimybox {
    val stt = GooglePlatformSpeechToText(context, Locale.ENGLISH)
    val tts = GooglePlatformTextToSpeech(context, Locale.ENGLISH)

    val dialogApi = AimyboxDialogApi(
        "",
        UUID.randomUUID().toString(),
        "https://bot.aimylogic.com/chatapi/webhook/zenbox/qhimZfMA:73238ad455cc45882e4a0c5ca6e4788342dd64ea",
        linkedSetOf(BeaconIdSkill)
    )

    return Aimybox(Config.create(stt, tts, dialogApi))
}

object BeaconIdSkill : CustomSkill<AimyboxRequest, AimyboxResponse> {
    var currentId: String? = null

    override suspend fun onRequest(request: AimyboxRequest): AimyboxRequest {
        if (currentId != null) {
            request.data?.put("id" to currentId)
        }
        return super.onRequest(request)
    }
}
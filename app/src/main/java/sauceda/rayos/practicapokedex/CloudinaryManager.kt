package sauceda.rayos.practicapokedex

import com.cloudinary.android.MediaManager
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.cloudinary.android.policy.UploadPolicy
import android.content.Context

object CloudinaryManager {

    fun init(context: Context) {
        // Configura con tus credenciales
        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = "dlfquqrlj"
        config["api_key"] = "453233749623459"
        config["api_secret"] = "WX3pRRftCxkwnn2BKnq66_2JdcA"

        // Inicializa el MediaManager de Cloudinary
        MediaManager.init(context, config)

    }

}
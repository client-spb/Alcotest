package kras.example.many.Screen.Result

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ResultShare {

    fun shareResult(context: Context, text: String) {
        val cacheDir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(cacheDir, "alkotester_rezultat.txt")
        file.writeText(text, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            setDataAndType(uri, "text/plain")
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri(null, uri)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться результатом"))
    }
}

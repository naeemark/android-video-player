package com.andromeda.kunalbhatia.demo.hungamaplayer.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

class GeneralUtils {

    companion object {

        fun dpToPx(mContext: Context, dp: Float): Int {
            val displayMetrics = mContext.resources.displayMetrics
            return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
        }
        fun dpToPxx(context: Context, dp: Int): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }

        fun getRealPathFromUri(context: Context?, contentUri: Uri): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && checkFilePathForN(
                    contentUri,
                    context
                ) != null) {
                return getFilePathForN(
                    contentUri,
                    context
                )
            } else {
                var result = ""
                var cursor: Cursor? = null
                try {
                    val proj = arrayOf(MediaStore.Images.Media.DATA)
                    cursor =
                        context?.getContentResolver()?.query(contentUri, proj, null, null, null)
                    assert(cursor != null)
                    val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    cursor.moveToFirst()
                    result = cursor.getString(column_index)
                } catch (e: Exception) {
                    result = ""
                } finally {
                    cursor?.close()
                }
                return result
            }
        }

        public fun checkFilePathForN(uri: Uri, context: Context?): Cursor?{
            return context?.contentResolver?.query(uri, null, null, null, null)
        }

        private fun getFilePathForN(uri: Uri, context: Context?): String {
            val returnCursor = context?.contentResolver?.query(uri, null, null, null, null)
            /*
     * Get the column indexes of the data in the Cursor,
     *     * move to the first row in the Cursor, get the data,
     *     * and display it.
     * */
            val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
            val file = File(context.filesDir, name)
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read = 0
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream!!.available()

//                val bufferSize = 1024
                val bufferSize = if (Math.min(bytesAvailable, maxBufferSize) > 0) {
                    Math.min(bytesAvailable, maxBufferSize)
                } else {
                    maxBufferSize
                }

                val buffers = ByteArray(bufferSize)
                while (read != -1) {
                    read = inputStream.read(buffers)
                    outputStream.write(buffers, 0, read)
                }
                Log.d("File Size", "Size " + file.length())
                inputStream.close()
                outputStream.close()
                Log.e("File Path", "Path " + file.path)
                Log.e("File Size", "Size " + file.length())
            } catch (e: Exception) {
                Log.e("Exception", e.message)
            }
            returnCursor.close()
            return file.path
        }


        fun toTextRequestBody(value: String): RequestBody {

            return RequestBody.create("text/plain".toMediaTypeOrNull(), value)
        }
    }

}
package com.andromeda.kunalbhatia.demo.hungamaplayer.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.*


class FileUtils {
    companion object{

        val VIDEO = "VIDEO"
        val PHOTO = "PHOTO"
        val FILE_SIZE_FOR_COMPRESSING_SMALL_FILES = 10

        public val rootPath : String = Environment.getExternalStorageDirectory().toString() + File.separator + ".Ballogy"
        public val storageVideoPath : String = rootPath + File.separator + "Videos" + File.separator
        public val storageImagePath : String = rootPath + File.separator + "Images" + File.separator
        public val compressedVideoFilePath : String = rootPath + File.separator + "CompressedVideos" + File.separator
        public val compressedImageFilePath : String = rootPath + File.separator + "CompressedImages" + File.separator

        public fun checkFileSizeIfGreaterThenAllowedSize(uri: Uri?): Boolean{
            val file = File(uri.toString())
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            if (fileSizeInMB > FILE_SIZE_FOR_COMPRESSING_SMALL_FILES) {
                return true
            }
            return false
        }

        public fun checkIsFileDurationIsMoreThanAllowed(context: Context, uriOfFile: Uri?): Boolean{
            if (getFileLength(context, uriOfFile) > 20000) {
                return true
            }
            return false
        }

        private fun getFileLength(context: Context, uriOfFile: Uri?): Long{
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uriOfFile)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
            retriever.release()
            return duration
        }

        public fun getFileMimeType(context: Context, uriOfFile: Uri?): String{
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uriOfFile)
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?:""
            retriever.release()
            return mimeType
        }

        public fun createFolderForMedia(){
            val videoStorage = File(storageVideoPath)
            if (!videoStorage.exists()) {
                videoStorage.mkdirs()
            }
            val imageStorage = File(storageImagePath)
            if (!imageStorage.exists()) {
                imageStorage.mkdirs()
            }
            val compressedVideoFolder = File(compressedVideoFilePath)
            if (!compressedVideoFolder.exists()) {
                compressedVideoFolder.mkdirs()
            }
            val compressedImageFolder = File(compressedImageFilePath)
            if (!compressedImageFolder.exists()) {
                compressedImageFolder.mkdirs()
            }
        }

        fun deleteFolderForMedia(fileOrDirectory: File){
//            val videoStorage = File(storageVideoPath)
//            if (videoStorage.exists()) {
//                videoStorage.delete()
//            }
//            val imageStorage = File(storageImagePath)
//            if (imageStorage.exists()) {
//                imageStorage.delete()
//            }
//            val compressedVideoFolder = File(compressedVideoFilePath)
//            if (compressedVideoFolder.exists()) {
//                compressedVideoFolder.delete()
//            }
//            val compressedImageFolder = File(compressedImageFilePath)
//            if (compressedImageFolder.exists()) {
//                compressedImageFolder.delete()
//            }

            if (fileOrDirectory.exists()) {
                if (fileOrDirectory.isDirectory()) {
                    if (!fileOrDirectory.listFiles().isNullOrEmpty()) {
                        for (child in fileOrDirectory.listFiles()) {
                            deleteFolderForMedia(child)
                        }
                    }
                }

                fileOrDirectory.delete()
            }
        }

        // for future use
        private fun getWidthHeightOfImage(uri: Uri): Array<Int> {
            var result = arrayOf<Int>(0,0)

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(File(uri.path).getAbsolutePath(), options)
            val imageHeight = options.outHeight
            val imageWidth = options.outWidth

            result[0] = imageWidth
            result[1] = imageHeight

            return result
        }

        public fun getVideoWidthOrHeight(file: File): Array<Int> {
            var result = arrayOf<Int>(0,0)
            var retriever : MediaMetadataRetriever ?= null
            var bmp : Bitmap ?= null
            var inputStream : FileInputStream?= null
            var mWidth : Int = 0
            var mHeight : Int = 0
            try {
                retriever = MediaMetadataRetriever();
                inputStream = FileInputStream(file.getAbsolutePath())
                retriever.setDataSource(inputStream.getFD())
                bmp = retriever.getFrameAtTime()
//                if (widthOrHeight.equals("width")){
                mWidth = bmp.getWidth()
//                }else {
                mHeight = bmp.getHeight()
//                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            } finally{
                if (retriever != null){
                    retriever.release()
                }
                if (inputStream != null){
                    inputStream.close()
                }
            }
            result[0] = mWidth
            result[1] = mHeight
            return result


        }

        fun convertWhatsAppImageToLocalFile(context: Context, uri: Uri): String{
            var roughBitmap: Bitmap
            try { // Works with content://, file://, or android.resource:// URIs
                val inputStream: InputStream? = context.contentResolver?.openInputStream(uri)
                roughBitmap = BitmapFactory.decodeStream(inputStream)
                val file : File? = bitmapConvertToFile(context, roughBitmap)
                return if (file == null) {
                    return ""
                } else {
                    return file.absolutePath
                }
            } catch (e: FileNotFoundException) { // Inform the user that things have gone horribly wrong
                Log.d("TAG", "convertWhatsAppImageToLocalFile Exp:\n"+e.message)
                return ""
            }
        }

        fun convertGooglePhotosVideoToLocalFile(context: Context, sourceuri: Uri, destinationFilename: String): String{
            val inputPFD = try {
                /*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 */
                context.contentResolver.openFileDescriptor(sourceuri, "r")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Log.e("TAG", "File not found.")
                return ""
            }

            // Get a regular file descriptor for the file
            val fd = inputPFD?.fileDescriptor

            var bis: BufferedInputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                bis = BufferedInputStream(FileInputStream(fd))
                bos = BufferedOutputStream(FileOutputStream(destinationFilename, false))
                val buf = ByteArray(1024)
                bis.read(buf)
                do {
                    bos.write(buf)
                } while (bis.read(buf) !== -1)
            } catch (e: Exception) {
                Log.d("TAG", "saveFile Exp:\n"+e.message)
            } finally {
                try {
                    if (bis != null) bis.close()
                    if (bos != null) bos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return destinationFilename
        }

        fun convertWhatsAppVideoToLocalFile(context: Context, uri: Uri): String{
            try {
//                val inputStream: InputStream? = context.contentResolver?.openInputStream(uri)
                var mimeType = getFileMimeType(context, uri)
                mimeType = mimeType.substring(mimeType.indexOf("/")+1, mimeType.length)
//                val uriString = uri.toString()
//                val extension: String? = uriString.substring(uriString.lastIndexOf("."))
                val file: File? = writeInputStreamToFile(context, uri, File(storageVideoPath + System.currentTimeMillis() + "." + mimeType))

                return file?.absolutePath ?: ""

            } catch (e: FileNotFoundException) { // Inform the user that things have gone horribly wrong
                Log.d("TAG", "convertWhatsAppImageToLocalFile Exp:\n"+e.message)
                return ""
            }
        }

        public fun bitmapConvertToFile(context: Context, bitmap: Bitmap?): File? {
            var fileOutputStream: FileOutputStream? = null
            var bitmapFile: File? = null
            try {
                val file = File(FileUtils.storageImagePath)
                if (!file.exists()) {
                    file.mkdir()
                }
                bitmapFile = File(file, "cropped_" + System.currentTimeMillis() + ".jpg")
                fileOutputStream = FileOutputStream(bitmapFile)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.flush()
                        fileOutputStream.close()
                        Log.d("TAG", "file saved")
                    } catch (e: Exception) {
                    }

                }
            }
            return bitmapFile
        }

        public fun writeInputStreamToFile(context: Context, inputUri: Uri, outputFile: File): File?{
            val inputStream : InputStream? = context.contentResolver.openInputStream(Uri.parse(inputUri.toString()))
            val outStream : OutputStream = FileOutputStream(outputFile)
            try {
                val buffer: ByteArray = ByteArray(1024)
                var length: Int? = 0

                while (true) {
                    length = inputStream?.read(buffer)
                    if (length != null) {
                        if (length <= 0)
                            break
                    }
                    if (length != null) {
                        outStream.write(buffer, 0, length)
                    }
                }


            }
            catch (e: Exception){
                Log.d("TAG", "writeInputStreamToFile Exp:\n"+e.message)
            }
            finally {
                outStream.flush()
                outStream.close()
                inputStream?.close()
            }
            return outputFile
        }


        fun getMimeType(
            context: Context,
            uri: Uri
        ): String? {
            var extension: String? = ""
            //Check uri format to avoid null
            extension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) { //If scheme is a content
                val mime = MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
            } else { //If scheme is a File
                //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
            }
            return extension
        }

        fun savefile(context: Context, sourceuri: Uri): String? {
            val inputPFD = try {
                /*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 */
                context.contentResolver.openFileDescriptor(sourceuri, "r")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Log.e("TAG", "File not found.")
                return null
            }

            // Get a regular file descriptor for the file
            val fd = inputPFD?.fileDescriptor

            val destinationFilename = storageImagePath + System.currentTimeMillis()+ ".jpg"
            var bis: BufferedInputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                bis = BufferedInputStream(FileInputStream(fd))
                bos = BufferedOutputStream(FileOutputStream(destinationFilename, false))
                val buf = ByteArray(1024)
                bis.read(buf)
                do {
                    bos.write(buf)
                } while (bis.read(buf) !== -1)
            } catch (e: Exception) {
                Log.d("TAG", "saveFile Exp:\n"+e.message)
            } finally {
                try {
                    if (bis != null) bis.close()
                    if (bos != null) bos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return destinationFilename
        }

        //region comment

        // calc exact destination size
//                val m = Matrix()
//                val inRect = RectF(0.0f, 0.0f, roughBitmap.width.toFloat(), roughBitmap.height.toFloat())
//                val outRect = RectF(0.0f, 0.0f, roughBitmap.width.toFloat(), roughBitmap.height.toFloat())
//                m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER)
//                val values = FloatArray(9)
//                m.getValues(values)
//                // resize bitmap if needed
//                val resizedBitmap: Bitmap = Bitmap.createScaledBitmap(
//                    roughBitmap,
//                    (roughBitmap.width * values[0]).toInt(),
//                    (roughBitmap.height * values[4]).toInt(),
//                    true
//                )
//                val name: String = "IMG_" + System.currentTimeMillis() + ".jpg"
//                val file: File = File(storageImagePath + name)
//                if (!file.exists()) {
//                    file.mkdir()
//                }
        //endregion
    }
}

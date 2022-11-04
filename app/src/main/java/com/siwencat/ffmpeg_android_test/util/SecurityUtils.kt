package com.siwencat.ffmpeg_android_test.util

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @Description TODO
 * @Creator ChenGuo
 * @Email wushengyuan1hao@163.com
 * @Date 11-04-2022 周五 14:51
 */
object SecurityUtils {
    fun getMD5(info: String): String {
        return try {
            val md5: MessageDigest = MessageDigest.getInstance("MD5")
            md5.update(info.toByteArray(charset("UTF-8")))
            val encryption: ByteArray = md5.digest()
            val strBuf = StringBuffer()
            for (i in encryption.indices) {
                if (Integer.toHexString(0xff and encryption[i].toInt()).length == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff and encryption[i].toInt()))
                } else {
                    strBuf.append(Integer.toHexString(0xff and encryption[i].toInt()))
                }
            }
            strBuf.toString()
        } catch (e: NoSuchAlgorithmException) {
            ""
        } catch (e: UnsupportedEncodingException) {
            ""
        }
    }
}
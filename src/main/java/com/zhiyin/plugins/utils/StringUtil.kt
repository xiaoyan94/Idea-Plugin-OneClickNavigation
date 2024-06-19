package com.zhiyin.plugins.utils

class StringUtil {

    companion object {

        /**
         * 首字母大写
         */
        fun String.toCamelCase(): String {
            return this.lowercase().replaceFirstChar { it.uppercase() }
        }


        /**
         * 下划线转驼峰
         */
        fun String.toSnakeCase(): String {
            return this.lowercase().replace("\\W+".toRegex(), "_").removeSuffix("_")
        }

        /**
         * 字符串转Unicode
         */
        @JvmStatic
        fun String.toUnicode(): String {
            return stringToUnicode(this)
        }

        /**
         * 字符串转Unicode
         */
        @JvmStatic
        fun stringToUnicode(string: String?): String {
            return string?.map {
                "\\u${it.code.toString(16).uppercase().padStart(4, '0')}"
            }?.joinToString("") ?: ""
        }

        /**
         * unicodeToString
         */
        @JvmStatic
        fun String?.unicodeToString(): String? {
            return this?.replace("\\\\u([0-9a-fA-F]{4})".toRegex()) {
                val hex = it.groupValues[1]
                val code = hex.toInt(16)
                String(Character.toChars(code))
            }
        }

    }
}
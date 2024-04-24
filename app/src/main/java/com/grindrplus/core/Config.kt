package com.grindrplus.core

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Config {
    private lateinit var configFilePath: String
    private lateinit var config: JSONObject

    fun initialize(context: Context) {
        configFilePath = File(context.filesDir, "grindrplus.json").path
        val file = File(configFilePath)
        if (!file.exists()) {
            try {
                file.createNewFile()
                val initialConfig = JSONObject().put("hooks", JSONObject())
                writeConfig(initialConfig)
            } catch (e: IOException) {
                Log.e("GrindrPlus", "Failed to create config file", e)
            }
        }
        config = readConfig(file)
    }

    private fun readConfig(file: File): JSONObject {
        return try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            Log.e("GrindrPlus", "Error reading config file", e)
            JSONObject().put("hooks", JSONObject())
        }
    }

    private fun writeConfig(json: JSONObject) {
        try {
            FileOutputStream(File(configFilePath)).use { fos ->
                fos.write(json.toString(4).toByteArray(Charsets.UTF_8))
            }
        } catch (e: IOException) {
            Log.e("GrindrPlus", "Failed to write config file", e)
        }
    }

    fun put(name: String, value: Any) {
        config.put(name, value)
        writeConfig(config)
    }

    fun get(name: String, default: Any): Any {
        return config.opt(name) ?: default
    }

    fun setHookEnabled(hookName: String, enabled: Boolean) {
        val hooks = config.optJSONObject("hooks") ?: JSONObject().also { config.put("hooks", it) }
        hooks.optJSONObject(hookName)?.put("enabled", enabled)
        writeConfig(config)
    }

    fun isHookEnabled(hookName: String): Boolean {
        val hooks = config.optJSONObject("hooks") ?: return false
        return hooks.optJSONObject(hookName)?.getBoolean("enabled") ?: false
    }

    fun initHookSettings(name: String, description: String, state: Boolean) {
        if (config.optJSONObject("hooks")?.optJSONObject(name) == null) {
            val hooks = config.optJSONObject("hooks") ?: JSONObject().also { config.put("hooks", it) }
            hooks.put(name, JSONObject().apply {
                put("description", description)
                put("enabled", state)
            })
            writeConfig(config)
        }
    }

    fun getHooksSettings(): Map<String, Pair<String, Boolean>> {
        val hooks = config.optJSONObject("hooks") ?: return emptyMap()
        val map = mutableMapOf<String, Pair<String, Boolean>>()

        val keys = hooks.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val obj = hooks.getJSONObject(key)
            map[key] = Pair(obj.getString("description"), obj.getBoolean("enabled"))
        }

        return map
    }
}

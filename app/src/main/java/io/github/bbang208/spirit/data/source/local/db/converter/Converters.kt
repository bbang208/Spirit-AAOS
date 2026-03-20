package io.github.bbang208.spirit.data.source.local.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.bbang208.spirit.data.models.GpsPoint
import io.github.bbang208.spirit.data.models.Sector
import io.github.bbang208.spirit.data.models.TelemetryPoint

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromGpsPointList(value: List<GpsPoint>): String = gson.toJson(value)

    @TypeConverter
    fun toGpsPointList(value: String): List<GpsPoint> {
        val type = object : TypeToken<List<GpsPoint>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromSectorList(value: List<Sector>): String = gson.toJson(value)

    @TypeConverter
    fun toSectorList(value: String): List<Sector> {
        val type = object : TypeToken<List<Sector>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromTelemetryPointList(value: List<TelemetryPoint>): String = gson.toJson(value)

    @TypeConverter
    fun toTelemetryPointList(value: String): List<TelemetryPoint> {
        val type = object : TypeToken<List<TelemetryPoint>>() {}.type
        return gson.fromJson(value, type)
    }
}

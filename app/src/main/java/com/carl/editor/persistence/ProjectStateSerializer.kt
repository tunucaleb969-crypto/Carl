package com.carl.editor.persistence

import androidx.compose.ui.graphics.Color
import com.carl.editor.EditorProjectState
import com.carl.editor.canvas.AspectRatioPreset
import com.carl.editor.canvas.CanvasSettings
import com.carl.editor.effects.ColorAdjustment
import com.carl.editor.effects.GlobalCrop
import com.carl.editor.effects.GlobalTransform
import com.carl.editor.timeline.Clip
import com.carl.editor.timeline.EditState
import org.json.JSONArray
import org.json.JSONObject

/**
 * Versioned, dependency-light project serialization.
 *
 * Project files deliberately store edit decisions and the source URI, not copied media bytes.
 * This keeps autosave small while allowing the original media to remain in MediaStore/storage.
 */
object ProjectStateSerializer {
    private const val SCHEMA_VERSION = 2

    fun toJson(projectName: String, sourceUri: String, state: EditorProjectState): String =
        JSONObject()
            .put("schemaVersion", SCHEMA_VERSION)
            .put("projectName", projectName)
            .put("sourceUri", sourceUri)
            .put("state", stateToJson(state))
            .toString()

    fun fromJson(json: String): SavedProject? = runCatching {
        val root = JSONObject(json)
        require(root.getInt("schemaVersion") == SCHEMA_VERSION) { "Unsupported project schema" }
        SavedProject(
            projectName = root.optString("projectName", "Untitled Project"),
            sourceUri = root.getString("sourceUri"),
            state = stateFromJson(root.getJSONObject("state"))
        )
    }.getOrNull()

    data class SavedProject(
        val projectName: String,
        val sourceUri: String,
        val state: EditorProjectState
    )

    private fun stateToJson(state: EditorProjectState): JSONObject =
        JSONObject()
            .put("clips", JSONArray().apply { state.clips.forEach { put(clipToJson(it)) } })
            .put("globalTransform", transformToJson(state.globalTransform))
            .put("globalCrop", cropToJson(state.globalCrop))
            .put("colorAdjustment", colorToJson(state.colorAdjustment))
            .put("canvasSettings", canvasToJson(state.canvasSettings))

    private fun stateFromJson(json: JSONObject): EditorProjectState =
        EditorProjectState(
            timeline = EditState(
                clips = json.getJSONArray("clips").let { array ->
                    buildList(array.length()) {
                        for (i in 0 until array.length()) add(clipFromJson(array.getJSONObject(i)))
                    }
                }
            ),
            globalTransform = transformFromJson(json.getJSONObject("globalTransform")),
            globalCrop = cropFromJson(json.getJSONObject("globalCrop")),
            colorAdjustment = colorFromJson(json.getJSONObject("colorAdjustment")),
            canvasSettings = canvasFromJson(json.getJSONObject("canvasSettings"))
        )

    private fun clipToJson(clip: Clip): JSONObject =
        JSONObject()
            .put("id", clip.id)
            .put("sourceStartMs", clip.sourceStartMs)
            .put("sourceEndMs", clip.sourceEndMs)
            .put("speed", clip.speed.toDouble())
            .put("transform", transformToJson(clip.transform))
            .put("crop", cropToJson(clip.crop))
            .put("color", colorToJson(clip.color))

    private fun clipFromJson(json: JSONObject): Clip =
        Clip(
            id = json.getString("id"),
            sourceStartMs = json.getLong("sourceStartMs"),
            sourceEndMs = json.getLong("sourceEndMs"),
            speed = json.getDouble("speed").toFloat(),
            transform = transformFromJson(json.getJSONObject("transform")),
            crop = cropFromJson(json.getJSONObject("crop")),
            color = colorFromJson(json.getJSONObject("color"))
        )

    private fun transformToJson(value: GlobalTransform): JSONObject =
        JSONObject()
            .put("rotationDegrees", value.rotationDegrees.toDouble())
            .put("flipHorizontal", value.flipHorizontal)
            .put("flipVertical", value.flipVertical)
            .put("zoom", value.zoom.toDouble())
            .put("panX", value.panX.toDouble())
            .put("panY", value.panY.toDouble())

    private fun transformFromJson(json: JSONObject): GlobalTransform =
        GlobalTransform(
            rotationDegrees = json.getDouble("rotationDegrees").toFloat(),
            flipHorizontal = json.getBoolean("flipHorizontal"),
            flipVertical = json.getBoolean("flipVertical"),
            zoom = json.getDouble("zoom").toFloat(),
            panX = json.getDouble("panX").toFloat(),
            panY = json.getDouble("panY").toFloat()
        )

    private fun cropToJson(value: GlobalCrop): JSONObject =
        JSONObject()
            .put("leftInset", value.leftInset.toDouble())
            .put("rightInset", value.rightInset.toDouble())
            .put("topInset", value.topInset.toDouble())
            .put("bottomInset", value.bottomInset.toDouble())

    private fun cropFromJson(json: JSONObject): GlobalCrop =
        GlobalCrop(
            leftInset = json.getDouble("leftInset").toFloat(),
            rightInset = json.getDouble("rightInset").toFloat(),
            topInset = json.getDouble("topInset").toFloat(),
            bottomInset = json.getDouble("bottomInset").toFloat()
        )

    private fun colorToJson(value: ColorAdjustment): JSONObject =
        JSONObject()
            .put("brightness", value.brightness.toDouble())
            .put("contrast", value.contrast.toDouble())
            .put("saturation", value.saturation.toDouble())

    private fun colorFromJson(json: JSONObject): ColorAdjustment =
        ColorAdjustment(
            brightness = json.getDouble("brightness").toFloat(),
            contrast = json.getDouble("contrast").toFloat(),
            saturation = json.getDouble("saturation").toFloat()
        )

    private fun canvasToJson(value: CanvasSettings): JSONObject =
        JSONObject()
            .put("aspectRatio", value.aspectRatio.name)
            .put("backgroundColor", value.backgroundColor.value.toString())

    private fun canvasFromJson(json: JSONObject): CanvasSettings =
        CanvasSettings(
            aspectRatio = runCatching {
                AspectRatioPreset.valueOf(json.getString("aspectRatio"))
            }.getOrDefault(AspectRatioPreset.ORIGINAL),
            backgroundColor = runCatching {
                Color(json.getString("backgroundColor").toULong())
            }.getOrDefault(Color.Black)
        )
}

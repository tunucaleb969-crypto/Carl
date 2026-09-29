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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProjectStateSerializerTest {
    @Test
    fun roundTrip_preserves_project_graph_and_visual_state() {
        val first = Clip(
            id = "clip-a",
            sourceStartMs = 250L,
            sourceEndMs = 5_250L,
            speed = 1.5f,
            transform = GlobalTransform(
                rotationDegrees = 90f,
                flipHorizontal = true,
                zoom = 1.75f,
                panX = -0.25f,
                panY = 0.4f
            ),
            crop = GlobalCrop(0.1f, 0.2f, 0.05f, 0.15f),
            color = ColorAdjustment(
                brightness = 0.2f,
                contrast = -0.15f,
                saturation = 22f
            )
        )
        val original = EditorProjectState(
            timeline = EditState(listOf(first)),
            globalTransform = GlobalTransform(rotationDegrees = 180f, zoom = 2f, panY = -0.3f),
            globalCrop = GlobalCrop(0.08f),
            colorAdjustment = ColorAdjustment(brightness = -0.1f, contrast = 0.25f, saturation = -30f),
            canvasSettings = CanvasSettings(AspectRatioPreset.RATIO_9_16, Color(0xFF11223344))
        )

        val json = try {
            ProjectStateSerializer.toJson("Demo", "content://video/1", original)
        } catch (error: Throwable) {
            throw AssertionError(
                "ProjectStateSerializer.toJson failed: " + error::class.qualifiedName + ": " + error.message,
                error
            )
        }
        assert(json.contains("backgroundColor")) { "Serialized project is missing canvas background color" }

        val restored = try {
            ProjectStateSerializer.fromJsonOrThrow(json)
        } catch (error: Throwable) {
            throw AssertionError(
                "ProjectStateSerializer.fromJsonOrThrow failed: " + error::class.qualifiedName + ": " + error.message,
                error
            )
        }

        assertNotNull(restored)
        assertEquals("Demo", restored!!.projectName)
        assertEquals("content://video/1", restored.sourceUri)
        assertEquals(original.timeline, restored.state.timeline)
        assertEquals(original.globalTransform, restored.state.globalTransform)
        assertEquals(original.globalCrop, restored.state.globalCrop)
        assertEquals(original.colorAdjustment, restored.state.colorAdjustment)
        assertEquals(original.canvasSettings.aspectRatio, restored.state.canvasSettings.aspectRatio)
        assertEquals(original.canvasSettings.backgroundColor.value, restored.state.canvasSettings.backgroundColor.value)
    }

    @Test
    fun invalid_schema_is_rejected() {
        val result = ProjectStateSerializer.fromJson(
            """{"schemaVersion":999,"projectName":"Bad","sourceUri":"content://bad","state":{}}"""
        )
        assertEquals(null, result)
    }
}

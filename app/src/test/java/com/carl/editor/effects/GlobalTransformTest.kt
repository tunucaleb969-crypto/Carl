package com.carl.editor.effects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalTransformTest {
    @Test
    fun defaultTransformProducesNoEffect() {
        assertTrue(GlobalTransform().toEffects().isEmpty())
    }

    @Test
    fun zoomIsClampedToSupportedRange() {
        assertEquals(1f, GlobalTransform(zoom = 0f).normalizedZoom)
        assertEquals(3f, GlobalTransform(zoom = 9f).normalizedZoom)
    }

    @Test
    fun panIsClampedToSupportedRange() {
        val transform = GlobalTransform(panX = -9f, panY = 9f)
        assertEquals(-1f, transform.normalizedPanX)
        assertEquals(1f, transform.normalizedPanY)
    }

    @Test
    fun zoomProducesAVisualEffect() {
        assertEquals(1, GlobalTransform(zoom = 1.5f).toEffects().size)
    }

    @Test
    fun panProducesAVisualEffect() {
        assertEquals(2, GlobalTransform(panX = 0.2f).toEffects().size)
    }

    @Test
    fun framingResetRestoresDefaultZoomAndPan() {
        val reset = GlobalTransform(zoom = 2.5f, panX = 0.5f, panY = -0.5f).resetFraming()
        assertEquals(1f, reset.normalizedZoom)
        assertEquals(0f, reset.normalizedPanX)
        assertEquals(0f, reset.normalizedPanY)
    }
}

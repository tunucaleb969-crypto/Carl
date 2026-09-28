package com.carl.editor.effects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalCropTest {
    @Test
    fun defaultCropProducesNoEffect() {
        assertTrue(GlobalCrop().toEffects().isEmpty())
    }

    @Test
    fun insetIsClampedToSupportedRange() {
        assertEquals(0f, GlobalCrop(-1f).normalizedInset)
        assertEquals(0.45f, GlobalCrop(1f).normalizedInset)
    }

    @Test
    fun positiveInsetProducesOneCropEffect() {
        assertEquals(1, GlobalCrop(0.2f).toEffects().size)
    }

    @Test
    fun excessiveInsetStillProducesOneCropEffect() {
        assertEquals(1, GlobalCrop(0.9f).toEffects().size)
    }
}

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
    fun symmetricInsetIsClampedToSupportedRange() {
        assertEquals(0f, GlobalCrop(-1f).normalizedInset)
        assertEquals(0.45f, GlobalCrop(1f).normalizedInset)
    }

    @Test
    fun positiveSymmetricInsetProducesOneCropEffect() {
        assertEquals(1, GlobalCrop(0.2f).toEffects().size)
    }

    @Test
    fun excessiveSymmetricInsetStillProducesOneCropEffect() {
        assertEquals(1, GlobalCrop(0.9f).toEffects().size)
    }

    @Test
    fun directionalInsetsAreIndependentlyRepresented() {
        val crop = GlobalCrop(leftInset = 0.1f, rightInset = 0.2f, topInset = 0.05f, bottomInset = 0.15f)
        assertEquals(0.1f, crop.normalizedLeft)
        assertEquals(0.2f, crop.normalizedRight)
        assertEquals(0.05f, crop.normalizedTop)
        assertEquals(0.15f, crop.normalizedBottom)
    }

    @Test
    fun directionalCropProducesOneEffect() {
        assertEquals(1, GlobalCrop(leftInset = 0.1f, rightInset = 0.2f, topInset = 0.05f, bottomInset = 0.15f).toEffects().size)
    }
}

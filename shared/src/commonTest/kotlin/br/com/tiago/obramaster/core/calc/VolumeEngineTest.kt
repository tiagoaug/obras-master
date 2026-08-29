package br.com.tiago.obramaster.core.calc

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class VolumeEngineTest {

    private fun assertProximo(esperado: Double, obtido: Double?, tolerancia: Double = 1e-4) {
        assertNotNull(obtido)
        assertEquals(true, abs(esperado - obtido) < tolerancia, "esperado $esperado, obtido $obtido")
    }

    @Test
    fun paralelepipedo_volume() {
        assertProximo(60.0, VolumeEngine.paralelepipedo(3.0, 4.0, 5.0))
        assertNull(VolumeEngine.paralelepipedo(0.0, 4.0, 5.0))
    }

    @Test
    fun cilindro_volume() {
        assertProximo(785.39816, VolumeEngine.cilindro(5.0, 10.0))
    }

    @Test
    fun esfera_volume() {
        assertProximo(523.59878, VolumeEngine.esfera(5.0))
    }

    @Test
    fun cone_volume() {
        assertProximo(261.79939, VolumeEngine.cone(5.0, 10.0))
    }

    @Test
    fun prisma_volume() {
        assertProximo(150.0, VolumeEngine.prisma(30.0, 5.0))
    }

    @Test
    fun troncoDePiramide_volume() {
        // bases 16 e 4, altura 6 → (6/3)*(16+4+8) = 56
        assertProximo(56.0, VolumeEngine.troncoDePiramide(16.0, 4.0, 6.0))
        assertNull(VolumeEngine.troncoDePiramide(0.0, 4.0, 6.0))
    }
}

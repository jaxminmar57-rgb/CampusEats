package com.jaz.myapplicationcampuseats.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.sin

/**
 * Genera y reproduce sonidos musicales usando síntesis de audio (sine waves).
 * Los sonidos se generan programáticamente — no necesita archivos de audio.
 */
object SoundManager {

    private val handler = Handler(Looper.getMainLooper())
    private const val SAMPLE_RATE = 22050

    // Notas musicales (frecuencias en Hz)
    private const val C4 = 261.63f
    private const val D4 = 293.66f
    private const val E4 = 329.63f
    private const val F4 = 349.23f
    private const val G4 = 392.00f
    private const val A4 = 440.00f
    private const val B4 = 493.88f
    private const val C5 = 523.25f
    private const val D5 = 587.33f
    private const val E5 = 659.26f
    private const val G5 = 783.99f

    // ── Sonidos públicos ─────────────────────────────────────────────────────

    /** Éxito — acorde mayor ascendente (C-E-G-C5) suave */
    fun playExito(context: Context) {
        playNotes(listOf(
            Note(C5, 80, 0.3f),
            Note(E5, 80, 0.3f),
            Note(G5, 120, 0.4f)
        ))
        hapticSuccess(context)
    }

    /** Error — dos notas menores descendentes */
    fun playError(context: Context) {
        playNotes(listOf(
            Note(E4, 100, 0.3f),
            Note(C4, 150, 0.25f)
        ))
        hapticError(context)
    }

    /** Agregar al carrito — "pop" agudo rápido */
    fun playAgregarCarrito(context: Context) {
        playNotes(listOf(
            Note(G4, 40, 0.2f),
            Note(C5, 60, 0.3f)
        ))
        hapticMedium(context)
    }

    /** Mensaje recibido — ding suave (2 notas) */
    fun playMensaje(context: Context) {
        playNotes(listOf(
            Note(E5, 70, 0.2f),
            Note(G5, 90, 0.25f)
        ))
        hapticLight(context)
    }

    /** Nuevo pedido para vendedor — fanfarria de 4 notas */
    fun playNuevoPedido(context: Context) {
        playNotes(listOf(
            Note(C4, 80, 0.3f),
            Note(E4, 80, 0.3f),
            Note(G4, 80, 0.3f),
            Note(C5, 160, 0.4f)
        ))
        hapticSuccess(context)
    }

    /** Toggle negocio — ascendente si abre, descendente si cierra */
    fun playToggle(context: Context, abierto: Boolean) {
        if (abierto) {
            playNotes(listOf(Note(C4, 60, 0.2f), Note(E4, 60, 0.2f), Note(G4, 90, 0.3f)))
        } else {
            playNotes(listOf(Note(G4, 60, 0.2f), Note(E4, 60, 0.2f), Note(C4, 90, 0.2f)))
        }
        hapticMedium(context)
    }

    /** Estrella de rating — nota sola proporcional */
    fun playEstrella(context: Context) {
        playNotes(listOf(Note(E5, 50, 0.15f)))
        hapticLight(context)
    }

    /** Tick de navegación — sutil */
    fun playTick(context: Context) {
        playNotes(listOf(Note(A4, 25, 0.08f)))
        hapticLight(context)
    }

    /** Enviar mensaje — whoosh ascendente rápido */
    fun playEnviar(context: Context) {
        playNotes(listOf(
            Note(G4, 30, 0.1f),
            Note(D5, 50, 0.15f)
        ))
        hapticMedium(context)
    }

    /** Click genérico — para botones, opciones de menú */
    fun playClick(context: Context) {
        playNotes(listOf(Note(A4, 20, 0.08f)))
        hapticLight(context)
    }

    /** Abrir menú lateral — barrido suave ascendente */
    fun playMenuOpen(context: Context) {
        playNotes(listOf(
            Note(C4, 30, 0.06f),
            Note(E4, 30, 0.08f),
            Note(G4, 40, 0.1f)
        ))
        hapticLight(context)
    }

    /** Cerrar menú — barrido descendente */
    fun playMenuClose(context: Context) {
        playNotes(listOf(
            Note(G4, 30, 0.08f),
            Note(E4, 30, 0.06f)
        ))
        hapticLight(context)
    }

    /** Notificación campana — para alertas in-app */
    fun playBell(context: Context) {
        playNotes(listOf(
            Note(E5, 60, 0.2f),
            Note(C5, 40, 0.15f),
            Note(E5, 80, 0.25f)
        ))
        hapticMedium(context)
    }

    /** Eliminar / vaciar — sonido de "poof" descendente */
    fun playEliminar(context: Context) {
        playNotes(listOf(
            Note(G4, 40, 0.15f),
            Note(D4, 50, 0.1f),
            Note(C4, 60, 0.08f)
        ))
        hapticMedium(context)
    }

    /** Confirmar pedido — fanfarria de celebración */
    fun playConfirmarPedido(context: Context) {
        playNotes(listOf(
            Note(C4, 60, 0.2f),
            Note(E4, 60, 0.25f),
            Note(G4, 60, 0.3f),
            Note(C5, 80, 0.35f),
            Note(E5, 120, 0.4f)
        ))
        hapticSuccess(context)
    }

    // ── Haptics ──────────────────────────────────────────────────────────────

    fun hapticLight(context: Context) = vibrar(context, 15L)
    fun hapticMedium(context: Context) = vibrar(context, 35L)
    fun hapticSuccess(context: Context) = vibrar(context, longArrayOf(0, 20, 40, 30, 40, 50))
    fun hapticError(context: Context) = vibrar(context, longArrayOf(0, 60, 40, 60, 40, 90))

    // ── Audio Engine (sine wave synthesis) ───────────────────────────────────

    private data class Note(val freq: Float, val durationMs: Int, val volume: Float)

    /**
     * Genera y reproduce una secuencia de notas musicales usando AudioTrack.
     * Cada nota es una onda sinusoidal pura con fade in/out para evitar clicks.
     */
    private fun playNotes(notes: List<Note>) {
        Thread {
            try {
                // Calcular tamaño total del buffer
                val totalSamples = notes.sumOf { (it.durationMs * SAMPLE_RATE) / 1000 + SAMPLE_RATE / 50 } // +gap
                val buffer = ShortArray(totalSamples)
                var offset = 0

                notes.forEach { note ->
                    val numSamples = (note.durationMs * SAMPLE_RATE) / 1000
                    val fadeLen = minOf(numSamples / 5, SAMPLE_RATE / 50) // fade = 20% o 20ms max

                    for (i in 0 until numSamples) {
                        val t = i.toFloat() / SAMPLE_RATE
                        var sample = sin(2.0 * PI * note.freq * t).toFloat()

                        // Fade in
                        if (i < fadeLen) sample *= i.toFloat() / fadeLen
                        // Fade out
                        if (i > numSamples - fadeLen) sample *= (numSamples - i).toFloat() / fadeLen

                        sample *= note.volume
                        buffer[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                    offset += numSamples
                    // Small gap between notes
                    val gap = SAMPLE_RATE / 50 // 20ms silence
                    offset += gap
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()

                // Release after playback
                val durationMs = notes.sumOf { it.durationMs } + notes.size * 20
                Thread.sleep(durationMs.toLong() + 50)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }.start()
    }

    // ── Vibración ────────────────────────────────────────────────────────────

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun vibrar(context: Context, ms: Long) {
        try {
            val v = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else { @Suppress("DEPRECATION") v.vibrate(ms) }
        } catch (_: Exception) {}
    }

    private fun vibrar(context: Context, pattern: LongArray) {
        try {
            val v = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else { @Suppress("DEPRECATION") v.vibrate(pattern, -1) }
        } catch (_: Exception) {}
    }
}

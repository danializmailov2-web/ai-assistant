package ru.assistant.ai

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.widget.Toast
import java.util.*

class AssistantService : Service() {

    private lateinit var tts: TextToSpeech
    private var callback: ((String) -> Unit)? = null

    companion object {
        private var instance: AssistantService? = null

        fun executeCommand(context: Context, command: String, result: (String) -> Unit) {
            instance?.let {
                it.callback = result
                it.processCommand(command)
            } ?: run {
                val intent = Intent(context, AssistantService::class.java)
                context.startService(intent)
                instance?.let {
                    it.callback = result
                    it.processCommand(command)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("ru", "RU")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun processCommand(command: String) {
        val cmd = command.lowercase(Locale.getDefault())

        when {
            cmd.contains("погод") -> getWeather()
            cmd.contains("новост") -> getNews()
            cmd.contains("курс") || cmd.contains("доллар") -> getCurrency()
            cmd.contains("сколько будет") || cmd.contains("посчитай") -> calculate(command)
            cmd.contains("заряд") || cmd.contains("батаре") -> getBattery()
            cmd.contains("цитат") || cmd.contains("афоризм") -> getQuote()
            cmd.contains("переведи") -> translate(command)
            else -> speak("Команда не распознана")
        }
    }

    private fun getWeather() {
        val result = "В Москве сейчас 22 градуса, ясно"
        callback?.invoke(result)
        speak(result)
    }

    private fun getNews() {
        val result = "Главные новости: В России запущен новый ИИ-помощник"
        callback?.invoke(result)
        speak(result)
    }

    private fun getCurrency() {
        val result = "Курс доллара: 92 рубля, евро: 100 рублей"
        callback?.invoke(result)
        speak(result)
    }

    private fun calculate(command: String) {
        val result = "Результат: 42"  // Заглушка
        callback?.invoke(result)
        speak(result)
    }

    private fun getBattery() {
        val result = "Заряд батареи: 75 процентов"
        callback?.invoke(result)
        speak(result)
    }

    private fun getQuote() {
        val quotes = listOf(
            "Жизнь — это то, что с тобой происходит, пока ты строишь планы. — Джон Леннон",
            "Будьте собой, остальные роли уже заняты. — Оскар Уайльд"
        )
        val result = quotes.random()
        callback?.invoke(result)
        speak(result)
    }

    private fun translate(command: String) {
        val result = "Перевод: Hello"  // Заглушка
        callback?.invoke(result)
        speak(result)
    }

    private fun speak(text: String) {
        callback?.invoke(text)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        instance = null
        tts.shutdown()
        super.onDestroy()
    }
}

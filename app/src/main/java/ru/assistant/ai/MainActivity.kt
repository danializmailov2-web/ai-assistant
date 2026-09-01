package ru.assistant.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var sendButton: Button
    private lateinit var micButton: Button
    private lateinit var statusText: TextView
    private lateinit var answerText: TextView
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация интерфейса
        inputText = findViewById(R.id.inputText)
        sendButton = findViewById(R.id.sendButton)
        micButton = findViewById(R.id.micButton)
        statusText = findViewById(R.id.statusText)
        answerText = findViewById(R.id.answerText)

        // Запрос разрешений
        checkPermissions()

        // TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("ru", "RU")
            }
        }

        // Кнопка "Отправить"
        sendButton.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isNotEmpty()) {
                processCommand(text)
                inputText.text.clear()
            }
        }

        // Кнопка "Микрофон"
        micButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите команду")
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val command = result?.firstOrNull()
            if (!command.isNullOrEmpty()) {
                inputText.setText(command)
                processCommand(command)
            }
        }
    }

    private fun processCommand(command: String) {
        statusText.text = "Думаю..."
        answerText.text = ""

        // Передача команды в сервис
        AssistantService.executeCommand(this, command) { result ->
            runOnUiThread {
                answerText.text = result
                statusText.text = "Готов"
                speak(result)
            }
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun checkPermissions() {
        val permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SET_ALARM,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )
        val needPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needPermissions.toTypedArray(), 200)
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}

package com.kurotkin.nfcdemo

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private val nfcTypes = HashSet<String>().apply {
        // Для запуска аctivity, если в метке содержится NDEF-сообщение. Он имеет самый высокий приоритет, и система будет запускать его в первую очередь.
        add(NfcAdapter.ACTION_NDEF_DISCOVERED)

        // Если никаких activity для intent ACTION_NDEF_DISCOVERED не зарегистрировано, то система распознавания попробует запустить приложение с этим intent.
        // Также этот intent будет сразу запущен, если найденное NDEF-сообщение не подходит под MIME-тип или URI, или метка совсем не содержит сообщения.
        add(NfcAdapter.ACTION_TECH_DISCOVERED)

        // Этот intent будет запущен, если два предыдущих intent не сработали.
        add(NfcAdapter.ACTION_TAG_DISCOVERED)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        prepareNFC()
    }

    private fun prepareNFC(){
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        val filters = nfcTypes.map { IntentFilter(it) }.toTypedArray()
        val techLists = arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, techLists)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            val ac = intent.action
            if(nfcTypes.contains(ac)){
                val tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
                val hexdump = tagId?.let { calcSerial(it) }
                Toast.makeText(this, "Сканирован NFC $hexdump", Toast.LENGTH_LONG).show()

                // EXTRA_TAG - объект Tag, описывающий отсканированную метку
                // EXTRA_NDEF_MESSAGES - массив NDEF-сообщений, просчитанный с метки
                // EXTRA_ID - низкоуровневый идентификатор метки
            }
        }
    }

    fun calcSerial(byteArr: ByteArray): String {
        return byteArr.map {
            Integer.toHexString(it.toInt() and 0xff)
        }.joinToString(separator = "") {
            if (it.length == 1) "0$it" else it
        }
    }
}
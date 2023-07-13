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
        add(NfcAdapter.ACTION_TECH_DISCOVERED)
        add(NfcAdapter.ACTION_TAG_DISCOVERED)
        add(NfcAdapter.ACTION_NDEF_DISCOVERED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        val nfcIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val filters = nfcTypes.map { IntentFilter(it) }.toTypedArray()
        //val filters = arrayOf(nfcIntentFilter)
        val techLists =
            arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))
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
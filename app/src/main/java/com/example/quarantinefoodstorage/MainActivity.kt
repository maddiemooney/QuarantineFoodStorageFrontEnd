package com.example.quarantinefoodstorage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.json.JSONObject
import java.io.Console
import java.lang.System.out


class MainActivity : AppCompatActivity() {
    lateinit var btnBarcode: Button
    lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Quarantine Food Storage Scanner"
        btnBarcode = findViewById(R.id.button)
        textView = findViewById(R.id.txtContent)
        btnBarcode.setOnClickListener {
            val intentIntegrator = IntentIntegrator(this@MainActivity)
            intentIntegrator.setBeepEnabled(false)
            intentIntegrator.setCameraId(0)
            intentIntegrator.setPrompt("SCAN")
            intentIntegrator.setBarcodeImageEnabled(false)
            intentIntegrator.initiateScan()
        }
    }
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show()
            } else {

                val queue = Volley.newRequestQueue(this)
                val url = "https://qfs-backend.azurewebsites.net/api/values/PostBarcodeGetItem"

                val paramsMap = HashMap<String, String>()
                paramsMap.put("barcode", result.contents)
                paramsMap.put("sku","temp")
                paramsMap.put("name","temp")
                paramsMap.put("dateadded","2020-01-01")
                paramsMap.put("quarantinelength","0")
                paramsMap.put("alertexists","0")
                paramsMap.put("locationid","0")

                val jsonParams = JSONObject(paramsMap as Map<*, *>)

                val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, jsonParams,
                    Response.Listener { response ->

                        System.out.println(response.toString())

                        val splitJson = response.toString().split(",").toTypedArray()
                        var barcode = ""
                        var sku = ""
                        var name = ""
                        splitJson.forEach{
                            val key = it.split(":")
                            if(key[0]=="{\"barcode\""){
                                barcode = key[1]
                                System.out.println(barcode)
                            }
                            else if(key[0]=="\"sku\""){
                                sku = key[1]
                                System.out.println(sku)
                            }
                            else if(key[0]=="\"name\""){
                                name = key[1]
                                System.out.println(name)
                            }
                            else{
                                //die
                            }
                        }

                        textView.text = String.format("Barcode: %s \nSKU: %s \nName: %s", barcode, sku, name)
                    },
                    Response.ErrorListener { error ->
                        error.printStackTrace()
                        textView.text = String.format("wrontg: %s", error)
                    }
                )
                queue.add(jsonObjectRequest)

                Log.d("MainActivity", "Scanned")
                Toast.makeText(this, "Scanned -> " + result.contents, Toast.LENGTH_SHORT)
                    .show()
                //textView.text = String.format("Scanned Result: %s", result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
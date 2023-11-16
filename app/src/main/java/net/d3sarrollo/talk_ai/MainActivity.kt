package net.d3sarrollo.talk_ai

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.util.Locale


class MainActivity : AppCompatActivity() {

    // creating variables on below line.

    private lateinit var queryEdt: TextInputEditText
    private lateinit var responseLV: ListView
    private val responseList = mutableListOf<String>()
    private lateinit var responseAdapter : ArrayAdapter<String>
    private val SPEECH_REQUEST_CODE = 0
    private lateinit var botonDictado:FloatingActionButton
    private lateinit var botonVoz:FloatingActionButton
    private lateinit var botonBuscar:FloatingActionButton

    private lateinit var textToSpeech: TextToSpeech
    private var textoaVoz = true
    var searchQuery: String = ""
    val highlightColor: Int = Color.YELLOW
    private lateinit var progressDialog: ProgressDialog




    var url = "https://api.openai.com/v1/completions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // initializing variables on below line.
        responseLV = findViewById(R.id.idLVResponse)
        queryEdt = findViewById(R.id.idEdtQuery)
        responseAdapter = ArrayAdapter(this, R.layout.list_item_custom_rojo, responseList)
        responseLV.adapter = responseAdapter
        botonDictado = findViewById(R.id.boton_dictado)
        botonVoz = findViewById(R.id.boton_a_voz)
        botonVoz.setColorFilter(getColor(R.color.azul2))
        botonBuscar = findViewById(R.id.boton_buscar)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando...")
        progressDialog.setCancelable(false)




        botonDictado.setOnClickListener(View.OnClickListener { //Get data from input field
            displaySpeechRecognizer()
        })

        botonBuscar.setOnClickListener(View.OnClickListener { //Get data from input field
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ingrese texto a resaltar")

            val input = EditText(this)
            builder.setView(input)

            builder.setPositiveButton("Buscar") { dialog, which ->
                val textoIngresado = input.text.toString()
                // hacer algo con el texto ingresado
                highlightSearchResults(responseLV,textoIngresado)
            }

            builder.setNegativeButton("Cancelar") { dialog, which ->
                dialog.cancel()
            }

            builder.show()
        })


        // create an object textToSpeech and adding features into it
        // create an object textToSpeech and adding features into it
        textToSpeech = TextToSpeech(applicationContext) { i ->
            // if No error is found then only it will run
            if (i != TextToSpeech.ERROR) {
                // To Choose language of speech
                textToSpeech.language = Locale.getDefault()
            }
        }

        botonVoz.setOnClickListener(View.OnClickListener { //Get data from input field
            if (textoaVoz){
                botonVoz.setColorFilter(getColor(R.color.black))
                textoaVoz = false
                Toast.makeText(this, "Text to speech disabled", Toast.LENGTH_LONG).show();
            } else {
                botonVoz.setColorFilter(getColor(R.color.azul2))
                textoaVoz = true
                Toast.makeText(this, "Text to speech activated", Toast.LENGTH_LONG).show();

            }
        })


        // adding editor action listener for edit text on below line.
        queryEdt.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // validating text
                if (queryEdt.text.toString().length > 0) {
                    // calling get response to get the response.
                    // creating a new list adapter and setting it to the ListView

                    getResponse(queryEdt.text.toString())
                    responseList.add("You: \n\n"+queryEdt.text.toString())
                    responseAdapter.notifyDataSetChanged()

                    queryEdt.setText("")

                    var color = true
                    val adapter = responseLV.adapter
                    for (i in 0 until adapter.count) {
                        val item = adapter.getItem(i)
                        val position = responseAdapter.getPosition(item.toString())
                        val view = responseLV.getChildAt(position)
                        if (view != null) {
                            if (color) {
                                view.setBackgroundColor(getColor(R.color.azul))
                                color = false
                            } else {
                                view.setBackgroundColor(getColor(R.color.morado1))
                                color = true
                            }
                        }
                    }


                } else {
                    Toast.makeText(this, "Please enter your query..", Toast.LENGTH_SHORT).show()
                    queryEdt.setText("")

                }

                return@OnEditorActionListener true
            }
            false
        })
    }
    private fun getResponse(query: String) {
        // setting text on for question on below line.
        // creating a queue for request queue.
        progressDialog.show()

        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        // creating a json object on below line.
        val jsonObject: JSONObject? = JSONObject()
        // adding params to json object.
        jsonObject?.put("model", "text-davinci-003")
        jsonObject?.put("prompt", query)
        jsonObject?.put("temperature", 0)
        jsonObject?.put("max_tokens", 2048)
        jsonObject?.put("top_p", 1)
        jsonObject?.put("frequency_penalty", 0.0)
        jsonObject?.put("presence_penalty", 0.0)

        // on below line making json object request.
        val postRequest: JsonObjectRequest =
            // on below line making json object request.
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response ->
                    // on below line getting response message and setting it to text view.
                    val responseMsg: String =
                        response.getJSONArray("choices").getJSONObject(0).getString("text")



                    responseMsg.trimStart()
                    responseList.add("AI: "+responseMsg)
                    responseAdapter.notifyDataSetChanged()

                    if (textoaVoz){
                        textToSpeech.speak(responseMsg,TextToSpeech.QUEUE_FLUSH,null);
                    }


                    var color = true
                    val adapter = responseLV.adapter
                    for (i in 0 until adapter.count) {
                        val item = adapter.getItem(i)
                        val position = responseAdapter.getPosition(item.toString())
                        val view = responseLV.getChildAt(position)
                        if (view != null) {
                            if (color) {
                                view.setBackgroundColor(getColor(R.color.azul))
                            } else {
                                view.setBackgroundColor(getColor(R.color.morado1))
                            }
                        }
                        if (color){
                            color = false
                        }else {
                            color = true
                        }
                    }
                    progressDialog.dismiss()


                },
                // adding on error listener
                Response.ErrorListener { error ->
                    Log.e("TAGAPI", "Error is : " + error.message + "\n" + error)
                    progressDialog.dismiss()

                }) {
                override fun getHeaders(): kotlin.collections.MutableMap<kotlin.String, kotlin.String> {
                    val params: MutableMap<String, String> = HashMap()
                    // adding headers on below line.
                    params["Content-Type"] = "application/json"
                    params["Authorization"] =
                        "Bearer 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"
                    return params;
                }
            }

        // on below line adding retry policy for our request.
        postRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // on below line adding our request to queue.
        queue.add(postRequest)
    }



    // Create an intent that can start the Speech Recognizer activity
    private fun displaySpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results!![0]
                }
            getResponse(spokenText.toString())
            responseList.add("You: \n\n"+spokenText.toString())
            responseAdapter.notifyDataSetChanged()


            var color = true
            val adapter = responseLV.adapter
            for (i in 0 until adapter.count) {
                val item = adapter.getItem(i)
                val position = responseAdapter.getPosition(item.toString())
                val view = responseLV.getChildAt(position)
                if (view != null) {
                    if (color) {
                        view.setBackgroundColor(getColor(R.color.azul))
                        color = false
                    } else {
                        view.setBackgroundColor(getColor(R.color.morado1))
                        color = true
                    }
                }
            }        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun highlightSearchResults(listView: ListView, searchQuery: String) {
        val adapter = responseLV.adapter
        for (i in 0 until adapter.count) {
            val item = adapter.getItem(i).toString()
            val index = item.indexOf(searchQuery, ignoreCase = true)
            if (index >= 0) {
                val spannableString = SpannableString(item)
                spannableString.setSpan(
                    ForegroundColorSpan(highlightColor),
                    index,
                    index + searchQuery.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val view = listView.getChildAt(i - listView.firstVisiblePosition)
                if (view != null) {
                    val textView = view.findViewById<TextView>(R.id.textViewListadito)
                    textView.text = spannableString

                }
            } else {
                val view = listView.getChildAt(i - listView.firstVisiblePosition)
                if (view != null) {
                    val textView = view.findViewById<TextView>(R.id.textViewListadito)
                    textView.text = item
                }
            }
        }
    }



}


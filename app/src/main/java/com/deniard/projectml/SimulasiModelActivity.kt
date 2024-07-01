package com.deniard.projectml

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimulasiModelActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private val mModelPath = "wine.tflite"

    private lateinit var resultText: TextView
    private lateinit var Fixed_Acidity: EditText
    private lateinit var Volatile_Acidity: EditText
    private lateinit var Citric_Acid: EditText
    private lateinit var Residual_Sugar: EditText
    private lateinit var chlorides: EditText
    private lateinit var Free_Sulfur_Dioxide: EditText
    private lateinit var Total_Sulfur_Dioxide: EditText
    private lateinit var Density: EditText
    private lateinit var pH: EditText
    private lateinit var Sulphates: EditText
    private lateinit var Alcohol: EditText
    private lateinit var Predict: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulasi_model)

        resultText = findViewById(R.id.txtResult)
        Fixed_Acidity = findViewById(R.id.FixedAcidity)
        Volatile_Acidity = findViewById(R.id.VolatileAcidity)
        Citric_Acid = findViewById(R.id.CitricAcide)
        Residual_Sugar = findViewById(R.id.ResidualSugar)
        chlorides = findViewById(R.id.Chlorides)
        Free_Sulfur_Dioxide = findViewById(R.id.FreeSulfurDioxide)
        Total_Sulfur_Dioxide = findViewById(R.id.TotalSulfurDioxide)
        Density = findViewById(R.id.Density)
        pH = findViewById(R.id.PH)
        Sulphates = findViewById(R.id.Sulphates)
        Alcohol = findViewById(R.id.Alcohol)
        Predict = findViewById(R.id.btnCheck)

        Predict.setOnClickListener {
            val result = doInference(
                Fixed_Acidity.text.toString(),
                Volatile_Acidity.text.toString(),
                Citric_Acid.text.toString(),
                Residual_Sugar.text.toString(),
                chlorides.text.toString(),
                Free_Sulfur_Dioxide.text.toString(),
                Total_Sulfur_Dioxide.text.toString(),
                Density.text.toString(),
                pH.text.toString(),
                Sulphates.text.toString(),
                Alcohol.text.toString()
            )
            runOnUiThread {
                resultText.text = if (result == 0) "Kualitas wine tidak bagus" else "Kualitas wine bagus"
            }
        }
        initInterpreter()
    }

    private fun initInterpreter() {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            setUseNNAPI(true)
        }
        interpreter = Interpreter(loadModelFile(assets, mModelPath), options)
    }

    private fun doInference(
        input1: String, input2: String, input3: String, input4: String, input5: String,
        input6: String, input7: String, input8: String, input9: String, input10: String, input11: String
    ): Int {
        val inputVal = FloatArray(11)
        try {
            inputVal[0] = input1.toFloat()
            inputVal[1] = input2.toFloat()
            inputVal[2] = input3.toFloat()
            inputVal[3] = input4.toFloat()
            inputVal[4] = input5.toFloat()
            inputVal[5] = input6.toFloat()
            inputVal[6] = input7.toFloat()
            inputVal[7] = input8.toFloat()
            inputVal[8] = input9.toFloat()
            inputVal[9] = input10.toFloat()
            inputVal[10] = input11.toFloat()
        } catch (e: NumberFormatException) {
            Log.e("Inference Error", "Invalid input format", e)
            return 0
        }

        Log.d("Input Values", inputVal.joinToString())

        val output = Array(1) { FloatArray(1) }
        interpreter.run(inputVal, output)

        Log.d("Model Output", output[0].toList().toString())

        return if (output[0][0] >= 0.04f) 1 else 0
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
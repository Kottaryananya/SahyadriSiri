package com.example.sahyadrisiri

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val seekClarity = findViewById<SeekBar>(R.id.seekClarity)
        val txtClarityValue = findViewById<TextView>(R.id.txtClarityValue)
        val radioFlow = findViewById<RadioGroup>(R.id.radioFlow)
        val radioSmell = findViewById<RadioGroup>(R.id.radioSmell)
        val radioHigh = findViewById<RadioButton>(R.id.radioHigh)
        val radioBad = findViewById<RadioButton>(R.id.radioBad)
        val checkPollution = findViewById<CheckBox>(R.id.checkPollution)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        seekClarity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtClarityValue.text = "Clarity: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSubmit.setOnClickListener {
            if (radioFlow.checkedRadioButtonId == -1 || radioSmell.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please select Flow and Smell", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val clarity = seekClarity.progress
            val flow = if (radioHigh.isChecked) "High" else "Low"
            val smell = if (radioBad.isChecked) "Bad" else "Normal"
            val pollution = checkPollution.isChecked

            val intent = Intent()
            intent.putExtra("clarity", clarity)
            intent.putExtra("flow", flow)
            intent.putExtra("smell", smell)
            intent.putExtra("pollution", pollution)

            MaterialAlertDialogBuilder(this)
                .setTitle("Thank You!")
                .setMessage("Your anonymous report helps protect the Sahyadri ecosystem.")
                .setCancelable(false)
                .setPositiveButton("Finish") { _, _ ->
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .show()
        }
    }
}

package com.example.sahyadrisiri

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AlertActivity : AppCompatActivity() {
    
    private lateinit var listView: ListView
    private val database = FirebaseDatabase.getInstance("https://sahyadrisiri-494604-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("reports")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alert)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listView = findViewById(R.id.listAlerts)
        
        setupFirebaseListener()
    }

    private fun setupFirebaseListener() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val unsafeReports = snapshot.children.mapNotNull { it.getValue(WaterReport::class.java) }
                    .filter { it.score < 4 }
                    .map { report ->
                        "⚠️ Unsafe Water Detected!\nScore: ${report.score}/10\nSmell: ${report.smell}\nPollution: ${if (report.pollution) "Yes" else "No"}"
                    }

                if (unsafeReports.isEmpty()) {
                    val emptyList = arrayListOf("✅ No unsafe water conditions reported in your area.")
                    listView.adapter = ArrayAdapter(this@AlertActivity, android.R.layout.simple_list_item_1, emptyList)
                } else {
                    val adapter = ArrayAdapter(this@AlertActivity, android.R.layout.simple_list_item_1, unsafeReports)
                    listView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AlertActivity", "Firebase Error: ${error.message}")
                Toast.makeText(this@AlertActivity, "Failed to load alerts", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

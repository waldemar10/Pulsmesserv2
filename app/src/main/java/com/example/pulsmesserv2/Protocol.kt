package com.example.pulsmesserv2

import MyViewModelFactory
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class ProtocolActivity : AppCompatActivity() {

    private val myViewModel: MyViewModel by viewModels { MyViewModelFactory(applicationContext) }

    private lateinit var lineChart: LineChart

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.fragment_protocol)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.protocol)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Setze den RecyclerView Adapter
        refreshRecyclerView()


        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

        }
        val btnDelete = findViewById<Button>(R.id.btnDelete)
         //CHECK IF USER SELECTED ITEMS TO REMOVE
        updateDeleteButtonStatus()
        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Selected Items")
            builder.setMessage("Are you sure you want to delete selected items?")
                .setCancelable(false)
                .setPositiveButton("yes"){
                        dialog,id->
                    // Lösche die ausgewählten Einträge
                    myViewModel.deleteSelectedItems("bpm_data.txt")
                    // Aktualisiere den RecyclerView Adapter
                    refreshRecyclerView()
                }
                .setNegativeButton("No"){
                        dialog,id->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()


        }
        lineChart = findViewById(R.id.lineChart)

        myViewModel.bpmList.observe(this) { bpmList ->
            updateLineChart(bpmList)
        }


    }
    fun updateDeleteButtonStatus(){
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        if(myViewModel.getItemsToRemove("bpm_data.txt")?.size!! > 0){
            btnDelete.isEnabled = true
            btnDelete.setBackgroundColor(resources.getColor(R.color.red))
        }else{
            btnDelete.isEnabled = false
            btnDelete.setBackgroundColor(resources.getColor(R.color.disabled))
        }
    }
    private fun updateLineChart(bpmList: ArrayList<BPMModel>) {

        val entries = bpmList?.mapIndexed { index, bpmModel ->
            Entry(index.toFloat(), bpmModel.bpm.toFloat())
        }

        val lineDataSet = LineDataSet(entries, "").apply {
//            lineWidth = 2.5f
//            color = resources.getColor(R.color.selected)
//            circleRadius = 4f
//            setCircleColor(resources.getColor(R.color.selected))
//
//            setDrawValues(false)
            color = resources.getColor(R.color.red)
            lineWidth = 2.5f
            setCircleColor(resources.getColor(R.color.red))
            circleRadius = 5f
            setDrawCircles(true)
            setDrawFilled(true)
            fillColor = resources.getColor(R.color.red)
            fillAlpha = 50
            setHighlightEnabled(true)
            highLightColor = resources.getColor(R.color.red)
            valueTextColor = resources.getColor(R.color.white)
            highlightLineWidth = 2f

        }
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // Ensure only whole numbers are displayed
        xAxis.valueFormatter = DateValueFormatter(bpmList.map {  it.date })
        xAxis.textColor = resources.getColor(R.color.white)

        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.gridColor = resources.getColor(R.color.white)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.axisLeft.textColor = resources.getColor(R.color.white)

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        val legend = lineChart.legend
        legend.isEnabled = false
        val description = Description()
        description.text = "BPM over time"
        description.textColor = resources.getColor(R.color.white)
        lineChart.description = description

        lineChart.invalidate()
    }
    inner class DateValueFormatter(private val dates: List<String>) : ValueFormatter() {

        private val inputDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        private val outputDateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

        override fun getFormattedValue(value: Float): String {
            println(dates)
            val index = value.toInt()
            return if (index >= 0 && index < dates.size) {
                try {
                    val date = inputDateFormat.parse(dates[index])
                    outputDateFormat.format(date ?: "")
                } catch (e: Exception) {
                    dates[index]
                }
            } else {
                value.toString()
            }
        }
    }
    fun refreshRecyclerView() {
        val model = myViewModel.readFromFile("bpm_data.txt")

//        if (model != null) {
//            myViewModel.setBpmList(model)
//            if (model.size > 0) {
//                val adapter = BPMModel_RecyclerViewAdapter(model, this, myViewModel)
//                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//                recyclerView.adapter = adapter
//                recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
//                recyclerView.adapter
//            }
//        }
    }

//    fun setFile(filename:String):ArrayList<BPMModel>?{
//
//        val model = myViewModel.readFromFile(filename)
//
//        refreshRecyclerView()
//        return model
//    }
}
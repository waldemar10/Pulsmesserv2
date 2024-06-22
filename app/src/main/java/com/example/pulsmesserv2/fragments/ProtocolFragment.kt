package com.example.pulsmesserv2.fragments

import android.annotation.SuppressLint
import com.example.pulsmesserv2.viewmodel.MyViewModelFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsmesserv2.BPMModel
import com.example.pulsmesserv2.BPMModel_RecyclerViewAdapter
import com.example.pulsmesserv2.MainActivity
import com.example.pulsmesserv2.viewmodel.MyViewModel
import com.example.pulsmesserv2.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class ProtocolFragment : Fragment(R.layout.fragment_protocol) {
    private val myViewModel: MyViewModel by activityViewModels { MyViewModelFactory(requireContext().applicationContext) }

    private lateinit var lineChart: LineChart
    private lateinit var btnDelete: Button
    private lateinit var btnBack: Button
    private lateinit var txtSize: TextView
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.protocol)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        refreshRecyclerView(view)

        initializeUI(view)
        updateDeleteButtonStatus()
        setupButtonListeners(view)

        myViewModel.bpmList.observe(viewLifecycleOwner) { bpmList ->
            updateLineChart(bpmList)
        }

    }


    private fun initializeUI(it: View){
        lineChart = it.findViewById(R.id.lineChart)
        btnDelete = it.findViewById(R.id.btnDelete)
        btnBack = it.findViewById(R.id.btnBack)

    }
    private fun setupButtonListeners(view: View){
        btnBack.setOnClickListener {
            ((requireActivity() as MainActivity)).navigateToHome()
        }
        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Löschung der ausgewählten Einträge")
            builder.setMessage("Bist du sicher, dass du die Einträge löschen möchtest?")
                .setCancelable(false)
                .setPositiveButton("Ja") { _, _ ->

                    // Lösche die ausgewählten Einträge
                    myViewModel.deleteSelectedItems("bpm_data.txt")
                    updateDeleteButtonStatus()
                    refreshRecyclerView(view)
                }
                .setNegativeButton("Nein") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()

        }
    }
    private fun updateDeleteButtonStatus() {

        if (myViewModel.getItemsToRemove("bpm_data.txt")?.size!! > 0) {
            btnDelete.isEnabled = true
            btnDelete.setBackgroundColor(resources.getColor(R.color.red, null))
        } else {
            btnDelete.isEnabled = false
            btnDelete.setBackgroundColor(resources.getColor(R.color.disabled, null))
        }

    }

    private fun updateLineChart(bpmList: ArrayList<BPMModel>) {
        val entries = bpmList.mapIndexed { index, bpmModel ->
            Entry(index.toFloat(), bpmModel.bpm.toFloat())
        }

        val lineDataSet = LineDataSet(entries, "").apply {
            color = resources.getColor(R.color.red, null)
            lineWidth = 2.5f
            setCircleColor(resources.getColor(R.color.red, null))
            circleRadius = 5f
            setDrawCircles(true)
            setDrawFilled(true)
            fillColor = resources.getColor(R.color.red, null)
            fillAlpha = 50
            setHighlightEnabled(true)
            highLightColor = resources.getColor(R.color.red, null)
            valueTextColor = resources.getColor(R.color.white, null)
            highlightLineWidth = 2f
        }

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = DateValueFormatter(bpmList.map { it.date })
        xAxis.textColor = resources.getColor(R.color.white, null)

        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.gridColor = resources.getColor(R.color.white, null)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.axisLeft.textColor = resources.getColor(R.color.white, null)

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        val legend = lineChart.legend
        legend.isEnabled = false
        val description = Description()
        description.text = "BPM over time"
        description.textColor = resources.getColor(R.color.white, null)
        lineChart.description = description

        lineChart.invalidate()
    }

    inner class DateValueFormatter(private val dates: List<String>) : ValueFormatter() {
        private val inputDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        private val outputDateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

        override fun getFormattedValue(value: Float): String {
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

    @SuppressLint("SetTextI18n")
    private fun refreshRecyclerView(view: View) {
        val model = myViewModel.readFromFile("bpm_data.txt")
        if (model != null) {
            myViewModel.setBpmList(model)
            if (model.size > 0) {
                val reversedModel = ArrayList(model.reversed())
                val adapter = BPMModel_RecyclerViewAdapter(
                    reversedModel, requireContext(), myViewModel, updateDeleteButtonCallback = { updateDeleteButtonStatus() })
                val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

                txtSize = view.findViewById(R.id.txtSize)
                txtSize.text = myViewModel.bpmList.value?.size.toString()+" / 100"

                recyclerView.adapter = adapter
                recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            }
        }
    }
}

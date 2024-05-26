package com.example.pulsmesserv2

import MyViewModelFactory
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var myViewModel: MyViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myViewModel = activityViewModels<MyViewModel> { MyViewModelFactory(context.applicationContext) }.value
    }

    private lateinit var lineChart: LineChart
    private lateinit var btnDelete: Button
    private lateinit var btnBack: Button
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.protocol)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setze den RecyclerView Adapter
        refreshRecyclerView(view)

        btnBack = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            ((requireActivity() as MainActivity)).navigateToHome()
//            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        btnDelete = view.findViewById(R.id.btnDelete)
        updateDeleteButtonStatus()
        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Löschung der ausgewählten Einträge")
            builder.setMessage("Bist du sicher, dass du die Einträge löschen möchtest?")
                .setCancelable(false)
                .setPositiveButton("Ja") { dialog, id ->
                    // Lösche die ausgewählten Einträge
                    myViewModel.deleteSelectedItems("bpm_data.txt")
                    // Aktualisiere den RecyclerView Adapter
                    refreshRecyclerView(view)
                }
                .setNegativeButton("Nein") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        lineChart = view.findViewById(R.id.lineChart)
        myViewModel.bpmList.observe(viewLifecycleOwner) { bpmList ->
            updateLineChart(bpmList)
        }
    }



    fun updateDeleteButtonStatus() {
    if(::myViewModel.isInitialized) {
        if (myViewModel.getItemsToRemove("bpm_data.txt")?.size!! > 0) {
            btnDelete.isEnabled = true
            btnDelete.setBackgroundColor(resources.getColor(R.color.red, null))
        } else {
            btnDelete.isEnabled = false
            btnDelete.setBackgroundColor(resources.getColor(R.color.disabled, null))
        }
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

    private fun refreshRecyclerView(view: View) {
        val model = myViewModel.readFromFile("bpm_data.txt")
        if (model != null) {
            myViewModel.setBpmList(model)
            if (model.size > 0) {
                val adapter = BPMModel_RecyclerViewAdapter(
                    model, requireContext(), myViewModel, updateDeleteButtonCallback = { updateDeleteButtonStatus() })
                val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            }
        }
    }
}

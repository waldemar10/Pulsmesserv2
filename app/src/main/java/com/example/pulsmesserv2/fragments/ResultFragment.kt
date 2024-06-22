package com.example.pulsmesserv2.fragments

import com.example.pulsmesserv2.viewmodel.MyViewModelFactory
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.pulsmesserv2.BPMModel
import com.example.pulsmesserv2.MainActivity
import com.example.pulsmesserv2.R
import com.example.pulsmesserv2.bluetooth.BluetoothDataListener
import com.example.pulsmesserv2.databinding.FragmentResultBinding
import com.example.pulsmesserv2.utils.ToastUtil
import com.example.pulsmesserv2.viewmodel.BpmViewModel
import com.example.pulsmesserv2.viewmodel.MyViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ResultFragment : Fragment(), BluetoothDataListener {

    private lateinit var btnSave: Button
    private lateinit var btnBack: Button
    private lateinit var txtPulse: TextView
    private val myViewModel: MyViewModel by viewModels { MyViewModelFactory(requireContext()) }
    private val bpmViewModel: BpmViewModel by activityViewModels()
    private lateinit var cvWarning: CardView
    private lateinit var tvWarningText: TextView
    private var bpmModelList: ArrayList<BPMModel> = ArrayList()
    private var _binding: FragmentResultBinding? = null
    private var currentMeasuring: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack = view.findViewById(R.id.btnBack)
        btnSave = view.findViewById(R.id.btnSave)
        txtPulse = view.findViewById(R.id.txtPulse)
        cvWarning = view.findViewById(R.id.cvWarning)
        tvWarningText = view.findViewById(R.id.tvWarningText)

        txtPulse.text = bpmViewModel.avgBpm.toString()

        btnBack.setOnClickListener {
            ((requireActivity() as MainActivity)).navigateToHome()
        }
        btnSave.setOnClickListener {
            saveData()
            ((requireActivity() as MainActivity)).navigateToHome()
        }
        bpmModelList = setFile("bpm_data.txt")!!
        setupWarningMessage()
    }
    private fun setFile(filename: String): ArrayList<BPMModel>? {
        val model = myViewModel.readFromFile(filename)
        return model
    }
    private fun saveData() {
        val filename = "bpm_data.txt"
        val date = LocalDateTime.now(ZoneId.of("Europe/Berlin"))
        val formattedDate = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

//        val bpm = num.text.toString().dropLast(4).toInt()
        val bpm = txtPulse.text.toString().toInt()
        val avgBpm = txtPulse.text.toString().toInt()
        val bpmModel = BPMModel(bpm,avgBpm, formattedDate, false)

        bpmModelList.add(bpmModel)
        myViewModel.saveToFile(filename, bpmModelList)
        ToastUtil.showToast(requireContext(), "Datei gespeichert")
    }

    @SuppressLint("SetTextI18n")
    fun setupWarningMessage() {


            var pulse = 0
            if(txtPulse.getText().contains("BPM",ignoreCase = true)) {
                pulse = txtPulse.text.toString().dropLast(4).toInt()
            }else{
                pulse = txtPulse.text.toString().toInt()
            }
       
            when {
                pulse > 100 -> {
                    cvWarning.visibility = CardView.VISIBLE
                    tvWarningText.text = "Ihre Herzfrequenz ist ungewöhnlich hoch." +
                            " Bitte setzen Sie sich hin, entspannen Sie sich und messen Sie Ihren Puls nach ein paar Minuten erneut. " +
                            "Wenn Ihre Herzfrequenz weiterhin erhöht bleibt, suchen Sie bitte einen Arzt auf."
                }

                pulse < 50 -> {
                    cvWarning.visibility = CardView.VISIBLE
                    tvWarningText.text = "Ihre Herzfrequenz ist ungewöhnlich niedrig. " +
                            "Bitte setzen Sie sich hin und ruhen Sie sich aus. Wenn Ihre Herzfrequenz weiterhin niedrig bleibt oder " +
                            "Sie sich unwohl fühlen, suchen Sie bitte umgehend einen Arzt auf."
                }

                else -> cvWarning.visibility = CardView.GONE
            }


    }

    override fun onCurrentValueReceived(value: Int) {
//        activity?.runOnUiThread {
//            txtPulse.text = value.toString()
//
//        }
    }

    override fun onAverageValueReceived(value: Int) {
       //
    }

    override fun onCurrentMeasuring(isMeasuring: Boolean) {
        currentMeasuring = isMeasuring
//        setupWarningMessage()
        if(!currentMeasuring){
            ((requireActivity() as MainActivity)).navigateToResult()
        }
    }

    override fun onActiveDevice(name: String) {
        TODO("Not yet implemented")
    }

    override fun onImageDeviceStatus(imageResources: Int) {
        TODO("Not yet implemented")
    }

    override fun onImageDeviceStatusColor(color: Int) {
        TODO("Not yet implemented")
    }

    override fun onCurrentBluetoothConnectionStatus(status: Boolean) {
        TODO("Not yet implemented")
    }

}
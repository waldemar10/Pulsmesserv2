package com.example.pulsmesserv2

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringReader

class MyViewModel(private val context: Context) : ViewModel() {
    val filename: String = "userData.txt"

    private val _bpmList = MutableLiveData<ArrayList<BPMModel>>()
    val bpmList: LiveData<ArrayList<BPMModel>> get() = _bpmList
    fun setBpmList(newList: ArrayList<BPMModel>) {
        _bpmList.value = newList
    }


    fun saveToFile(filename: String, fileContents: ArrayList<BPMModel>) {
        val gson = Gson()
        val jsonString = gson.toJson(fileContents)
        println(jsonString)

        try {
            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readFromFile(filename: String): ArrayList<BPMModel>? {
        val gson = Gson()
        return try {
            val file = context.getFileStreamPath(filename)
            if (file == null || !file.exists()) {
                return ArrayList()
            }

            val fileInputStream = context.openFileInput(filename)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val jsonStringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                jsonStringBuilder.append(line)
            }
            fileInputStream.close()

            val jsonString = jsonStringBuilder.toString()
            val type = object : TypeToken<ArrayList<BPMModel>>() {}.type

            val jsonReader = JsonReader(StringReader(jsonString))
            jsonReader.isLenient = true

            gson.fromJson(jsonReader, type)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun getItemsToRemove(filename: String): ArrayList<BPMModel>? {
        val bpmList = readFromFile(filename)
        if(bpmList != null){
            val itemsToRemove = bpmList.filter { it.selected }
            return itemsToRemove as ArrayList<BPMModel>
        }else{
            return null
        }
    }
    fun deleteSelectedItems(filename: String): ArrayList<BPMModel>? {
        println("deleteSelectedItems")
        val bpmList = readFromFile(filename)
        if (bpmList != null) {
            val itemsToRemove = bpmList.filter { it.selected }

            if (itemsToRemove.isNotEmpty()) {
                bpmList.removeAll(itemsToRemove.toSet())
                println("remove items")
                Toast.makeText(context, "Items deleted", Toast.LENGTH_SHORT).show()
            }

            saveToFile(filename, bpmList)

        }
        return bpmList
    }
    fun deleteOneItem(filename: String, index: Int) {
//        val bpmList = readFromFile(filename)
//        bpmList.removeAt(index)
//        saveToFile(filename, bpmList)
    }

}
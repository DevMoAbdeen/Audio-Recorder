package com.msa.audiorecorder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.msa.audiorecorder.databinding.FragmentBottomDialogBinding
import com.msa.audiorecorder.models.Recordes
import com.msa.audiorecorder.room_db.database.DatabaseClient
import java.text.SimpleDateFormat
import java.util.*

class BottomDialogFragment(var onRecordListener: OnRecordListener) : BottomSheetDialogFragment() {
    interface OnRecordListener{
        fun updateRecord(index: Int, newName: String)
    }

    lateinit var dialogBinding: FragmentBottomDialogBinding
    private var isSavedRecord = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialogBinding = FragmentBottomDialogBinding.inflate(inflater, container, false)

        val recordDialog = requireActivity().getSharedPreferences("RecordDialog", AppCompatActivity.MODE_PRIVATE)

        if (Recordes.saveOrRename == "renameRecord") {
            isSavedRecord = true
            dialogBinding.tvTitle.setText("Update record name ?")
            dialogBinding.etRecordName.setText(recordDialog.getString("nameRecord", "").toString())
            dialogBinding.btnSaveRecord.text = "Update Record"
        }else{
            dialogBinding.etRecordName.setText("${SimpleDateFormat("yyyy/MM/dd - HH:mm:SS").format(Date())}")
        }

        dialogBinding.btnSaveRecord.setOnClickListener {
            val nameRecord = dialogBinding.etRecordName.text.toString().trim()
            if (nameRecord.isEmpty()) {
                dialogBinding.etRecordName.setError("Write name !!")
            }else {
                isSavedRecord = true
                if (Recordes.saveOrRename == "renameRecord") {
                    val id = recordDialog.getInt("idRecord", 0)
                    val index = recordDialog.getInt("indexRecord", 0)

                    onRecordListener.updateRecord(index, nameRecord)
                    DatabaseClient.getInstance(requireContext())!!.appDatabase.recordDao()
                        .editRecordeName(id, nameRecord)
                    Toast.makeText(requireContext(), "Record is updated", Toast.LENGTH_SHORT).show()
                } else {
                    val date = SimpleDateFormat("yyyy/MM/dd - HH:mm:SS").format(Date())

                    val record = Recordes()
                    record.path = Recordes.pathRecord
                    record.name = nameRecord
                    record.date = date
                    record.timer = Recordes.timerRecord

                    DatabaseClient.getInstance(requireContext())!!.appDatabase.recordDao()
                        .insertRecorde(record)
                    Toast.makeText(requireContext(), "Record $nameRecord is saved.", Toast.LENGTH_SHORT).show()
                }
                dismiss()
            }
        }

        return dialogBinding.root
    }

    override fun onPause() {
        super.onPause()
        if(! isSavedRecord){
            val date = SimpleDateFormat("yyyy/MM/dd - HH:mm:SS").format(Date())

            val record = Recordes()
            record.path = Recordes.pathRecord
            record.name = dialogBinding.etRecordName.text.toString()
            record.date = date
            record.timer = Recordes.timerRecord

            DatabaseClient.getInstance(requireContext())!!.appDatabase.recordDao()
                .insertRecorde(record)
            Toast.makeText(requireContext(), "Record ${dialogBinding.etRecordName.text.toString()} is saved.", Toast.LENGTH_SHORT).show()
        }
    }


}
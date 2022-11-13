package com.msa.audiorecorder.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.msa.audiorecorder.R
import com.msa.audiorecorder.adapters.MyRecordesAdapter
import com.msa.audiorecorder.databinding.FragmentRecordesDialogBinding
import com.msa.audiorecorder.models.Recordes
import com.msa.audiorecorder.room_db.database.DatabaseClient
import java.io.File
import java.lang.Exception

class RecordesDialogFragment(var onRecordListener: OnRecordListener) : BottomSheetDialogFragment() {
    interface OnRecordListener{
        fun deleteRecord(index: Int)
        fun renameRecord(index: Int, newName: String)
    }
    lateinit var recordesBinding: FragmentRecordesDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recordesBinding = FragmentRecordesDialogBinding.inflate(inflater, container, false)

        val recordDialog = requireActivity().getSharedPreferences("RecordDialog", AppCompatActivity.MODE_PRIVATE)

        recordesBinding.renameRecord.setOnClickListener {
            Recordes.saveOrRename = "renameRecord"
            val bottomDialog = BottomDialogFragment(object: BottomDialogFragment.OnRecordListener{
                override fun updateRecord(index: Int, newName: String) {
                    onRecordListener.renameRecord(index, newName)
                }

            })
            bottomDialog.show(requireActivity().supportFragmentManager, "renameRecord")
            dismiss()
        }

        recordesBinding.shareRecord.setOnClickListener {
            val path = recordDialog.getString("pathRecord", "").toString()
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "audio/mp3"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
            startActivity(Intent.createChooser(intent, "Share recorde..."))

        }

        recordesBinding.deleteRecord.setOnClickListener {
            val id = recordDialog.getInt("idRecord", 0)
            val index = recordDialog.getInt("indexRecord", 0)

            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Delete Record !?")
            alert.setMessage("Are you sure you want to delete the recording !?")
            alert.setIcon(R.drawable.ic_delete)
            alert.setCancelable(true)

            alert.setPositiveButton("Yes") { d, i ->
                val recordPath: String = DatabaseClient.getInstance(requireContext())!!.appDatabase.recordDao()
                    .getRecordPath(id)

                onRecordListener.deleteRecord(index)
                DatabaseClient.getInstance(requireContext())!!.appDatabase.recordDao().deleteRecord(id)
                try {
                    File(recordPath).delete()
                }catch (ex: Exception){
                    Toast.makeText(requireContext(), "${ex.message}", Toast.LENGTH_SHORT).show()
                }

                Toast.makeText(requireContext(), "Record Delete", Toast.LENGTH_SHORT).show()
                d.dismiss()
                dismiss()
            }

            alert.setNegativeButton("Cancel") { d, i ->
                d.cancel()
            }
            alert.create().show()

        }

        return recordesBinding.root
    }

}
package com.msa.audiorecorder.adapters

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.msa.audiorecorder.activities.AllRecordesActivity
import com.msa.audiorecorder.activities.PlayRecordActivity
import com.msa.audiorecorder.databinding.RvRecordesListBinding
import com.msa.audiorecorder.fragments.BottomDialogFragment
import com.msa.audiorecorder.fragments.RecordesDialogFragment
import com.msa.audiorecorder.models.Recordes

class MyRecordesAdapter(var activity: Activity, var data: MutableList<Recordes>):
    RecyclerView.Adapter<MyRecordesAdapter.RecordesViewHolder>(){

    inner class RecordesViewHolder(var binding: RvRecordesListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordesViewHolder {
        return RecordesViewHolder(RvRecordesListBinding.inflate(LayoutInflater.from(activity), parent, false))
    }

    override fun onBindViewHolder(holder: RecordesViewHolder, position: Int) {
        holder.binding.tvRecordeName.text = data[position].name
        holder.binding.tvRecordeDate.text = data[position].date
        holder.binding.tvRecordeTime.text = data[position].timer

        holder.binding.playRecorde.setOnClickListener {
            val intent = Intent(activity, PlayRecordActivity::class.java)
            intent.putExtra("filePath", data[position].path)
            intent.putExtra("fileName", data[position].name)
            activity.startActivity(intent)
        }

//        holder.binding.cbSelected.setOnClickListener {
//            holder.binding.cbSelected.isChecked = ! (holder.binding.cbSelected.isChecked)
//            // الفحص والاضافة او الحذف بكون بعد التغيير
//            if(holder.binding.cbSelected.isChecked){
//                selectItemsId.add(data[position].Id)
//            }else{
//                selectItemsId.remove(data[position].Id)
//            }
//        }

        holder.binding.playRecorde.setOnLongClickListener {
            val sharedPreferences = activity.getSharedPreferences("RecordDialog", AppCompatActivity.MODE_PRIVATE)
            val sharedDialog = sharedPreferences.edit()
            sharedDialog.putInt("idRecord", data[position].Id)
            sharedDialog.putString("pathRecord", data[position].path)
            sharedDialog.putInt("indexRecord", position)
            sharedDialog.apply()

            val recordDialog = RecordesDialogFragment(object: RecordesDialogFragment.OnRecordListener{
                override fun deleteRecord(index: Int) {
                    if(data.isNotEmpty()) {
                        data.removeAt(index)
                        notifyDataSetChanged()
                    }
                }

                override fun renameRecord(index: Int, newName: String) {
                    if(data.isNotEmpty()) {
                        data[index].name = newName
                        notifyDataSetChanged()
                    }
                }

            })
            val allRecordes = activity as AllRecordesActivity
            recordDialog.show(allRecordes.supportFragmentManager, "recordDialog")

            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}
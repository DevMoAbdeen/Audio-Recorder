package com.msa.audiorecorder.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.msa.audiorecorder.R
import com.msa.audiorecorder.adapters.MyRecordesAdapter
import com.msa.audiorecorder.databinding.ActivityAllRecordesBinding
import com.msa.audiorecorder.models.Recordes
import com.msa.audiorecorder.room_db.database.DatabaseClient
import java.io.File
import java.lang.Exception

class AllRecordesActivity() : AppCompatActivity() {
    lateinit var binding: ActivityAllRecordesBinding
    private var recordes = ArrayList<Recordes>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllRecordesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }


        getAllRecords()

        binding.etSearch.addTextChangedListener {
            val searchNotes = ArrayList<Recordes>()
            val search = binding.etSearch.text.toString()
            if(search.isNotEmpty()){
                // البحث بكون من الArrayList عشان البحث يكون سريع.. وببحث عن التسجيل بالاسم
                for(i in 0 until recordes.size){
                    if(recordes[i].name!!.contains(search, true)){
                        searchNotes.add(recordes[i])
                    }
                }
                if(searchNotes.isEmpty()){
                    binding.recordesRecyclerView.visibility = View.GONE
                    binding.tvEmpty.setText("There are no records containing this name !")
                    binding.linearIfImpty.visibility = View.VISIBLE
                }else{
                    binding.recordesRecyclerView.visibility = View.VISIBLE
                    binding.linearIfImpty.visibility = View.GONE

                    val recordesAdapter = MyRecordesAdapter(this, searchNotes)
                    binding.recordesRecyclerView.adapter = recordesAdapter
                    binding.recordesRecyclerView.layoutManager = LinearLayoutManager(this)
                }
            }else{
                checkData()
            }
        }

    }

    private fun getAllRecords() {
        recordes = DatabaseClient.getInstance(this@AllRecordesActivity)!!.appDatabase.recordDao()
            .getAllRecordes() as ArrayList<Recordes>

        checkData()
    }

    private fun checkData(){
        if(recordes.isEmpty()){
            binding.recordesRecyclerView.visibility = View.GONE
            binding.linearIfImpty.visibility = View.VISIBLE
        }else{
            binding.recordesRecyclerView.visibility = View.VISIBLE
            binding.linearIfImpty.visibility = View.GONE

            val recordesAdapter = MyRecordesAdapter(this, recordes)
            binding.recordesRecyclerView.adapter = recordesAdapter
            binding.recordesRecyclerView.layoutManager = LinearLayoutManager(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_all_recordes, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.deleteAllRecodes -> {
                if(recordes.isNotEmpty()) {
                    val alert = AlertDialog.Builder(this)
                    alert.setTitle("Delete All Recordings !?")
                    alert.setMessage("You can not recover recordings after they have been deleted," +
                            " Are you want delete it !?")
                    alert.setIcon(R.drawable.ic_delete)
                    alert.setCancelable(true)

                    alert.setPositiveButton("Yes") { d, i ->
                        DatabaseClient.getInstance(this@AllRecordesActivity)!!.appDatabase.recordDao()
                            .deleteAllRecordes()

                        try {
                            for (i in recordes.indices) {
                                File(recordes[i].path).delete()
                            }
                        } catch (ex: Exception) {
                            Toast.makeText(this, "${ex.message}", Toast.LENGTH_SHORT).show()
                        }

                        Toast.makeText(this@AllRecordesActivity, "All recodes are deleted", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@AllRecordesActivity, MainActivity::class.java))
                        finish()
                    }

                    alert.setNegativeButton("Cancel") { d, i ->
                        d.cancel()
                    }
                    alert.create().show()
                }else{
                    Toast.makeText(this@AllRecordesActivity, "No Recordings !!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
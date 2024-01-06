package com.hasanbilgin.artbookkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasanbilgin.artbookkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artList: ArrayList<Art>
    private lateinit var artAdapter: ArtAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artList = ArrayList<Art>()
        artAdapter = ArtAdapter(artList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = artAdapter
        getList()


    }

    private fun getList() {
        try {
            val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)
            val cursor = database.rawQuery("SELECT *FROM arts", null)
            val artNameIndex = cursor.getColumnIndex("artname")
            val idIndex = cursor.getColumnIndex("id")
            while (cursor.moveToNext()) {
                val name = cursor.getString(artNameIndex)
                val id = cursor.getInt(idIndex)
                val art = Art(name, id)
                artList.add(art)
            }
            //artadapetere haber ver arraylist değişti yenileri recycleylerview güncellesin
            artAdapter.notifyDataSetChanged()

            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // menü için
    //bağlama için
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu, menu)
        return super.onCreateOptionsMenu(menu)

    }

    //menü için
    //tıklandığında ki metot
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_art_item) {
            val intent = Intent(this@MainActivity, ArtActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}
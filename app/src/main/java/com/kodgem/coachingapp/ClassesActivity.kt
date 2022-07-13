package com.kodgem.coachingapp

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.kodgem.coachingapp.databinding.ActivityClassesBinding


class ClassesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val mathButton = binding.mathButton
        mathButton.setOnClickListener {

            //create instance of PopupMenu
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.math_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.kumeler) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("studyType", "Kümeler")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.mantik) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("studyType", "Mantık")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.denklemlerVeEsitsizlikler) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("studyType", "Denklemler ve Eşitsizlikler")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.ucgenler) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("studyType", "Üçgenler")
                    this.startActivity(intent)
                }
                if (item.itemId == R.id.veri) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("studyType", "Veri")
                    this.startActivity(intent)
                }
                false
            }
        }


    }


}


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
        val fizik = binding.physics
        val turkce = binding.turkce
        val cografya = binding.cografya
        val biyoloji = binding.biyoloji
        val kimya = binding.kimya
        val tarih = binding.tarih
        val geometri = binding.geometri

        mathButton.setOnClickListener {

            //create instance of PopupMenu
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Matematik")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Matematik")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }

        geometri.setOnClickListener {
            //create instance of PopupMenu
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Geometri")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Geometri")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }

        fizik.setOnClickListener {
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Fizik")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Fizik")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }
        turkce.setOnClickListener {
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Türkçe-Edebiyat")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Türkçe-Edebiyat")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }
        cografya.setOnClickListener {
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Coğrafya")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Coğrafya")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }
        biyoloji.setOnClickListener {
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Biyoloji")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Biyoloji")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }
        kimya.setOnClickListener {
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Kimya")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Kimya")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }
        tarih.setOnClickListener {
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Tarih")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Tarih")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }


    }


}


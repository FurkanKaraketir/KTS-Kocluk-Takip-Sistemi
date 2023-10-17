package com.karaketir.coachingapp

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.karaketir.coachingapp.databinding.ActivityClassesBinding


class ClassesActivity : AppCompatActivity() {

    init {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }


    private lateinit var binding: ActivityClassesBinding
    private var kurumKodu = 0
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
        val felsefe = binding.felsefe
        val din = binding.din
        val deneme = binding.deneme
        val denemeAsil = binding.denemeAsil
        val paragrafButton = binding.paragrafButton
        val problemButton = binding.problemButton
        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()


        paragrafButton.setOnClickListener {
            val intent = Intent(this, AddPrgrphPrblmActivity::class.java)
            intent.putExtra("kurumKodu", kurumKodu.toString())
            intent.putExtra("dersAdi", "Paragraf")
            this.startActivity(intent)
        }

        problemButton.setOnClickListener {
            val intent = Intent(this, AddPrgrphPrblmActivity::class.java)
            intent.putExtra("kurumKodu", kurumKodu.toString())
            intent.putExtra("dersAdi", "Problem")
            this.startActivity(intent)
        }


        mathButton.setOnClickListener {

            //create instance of PopupMenu
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("dersAdi", "Matematik")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("kurumKodu", kurumKodu.toString())
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
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("dersAdi", "Geometri")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("dersAdi", "Geometri")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }

        felsefe.setOnClickListener {

            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("dersAdi", "Felsefe")
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("dersAdi", "Felsefe")
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }

        din.setOnClickListener {

            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Din")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Din")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
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
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Fizik")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
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
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Türkçe-Edebiyat")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
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
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Coğrafya")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
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
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Biyoloji")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
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
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Kimya")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
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
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Tarih")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }

        deneme.setOnClickListener {
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Diğer")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "TYT")
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Diğer")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }
        denemeAsil.setOnClickListener {
            val popup = PopupMenu(applicationContext, it)
            //inflate menu with layout mainmenu
            popup.inflate(R.menu.subject_context)
            popup.show()

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.TYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Denemeler")
                    intent.putExtra("studyType", "TYT")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    this.startActivity(intent)
                }

                if (item.itemId == R.id.AYT) {
                    val intent = Intent(this, EnterStudyActivity::class.java)
                    intent.putExtra("dersAdi", "Denemeler")
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    intent.putExtra("studyType", "AYT")
                    this.startActivity(intent)
                }
                false
            }
        }


    }


}


package com.karaketir.coachingapp.adapter

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.coachingapp.databinding.KonuYanlisEditTextRowBinding
import com.karaketir.coachingapp.models.SubItem


open class DenemeKonulariRecyclerAdapter(
    private val konuListesi: ArrayList<SubItem>, val dersAdi: String
) : RecyclerView.Adapter<DenemeKonulariRecyclerAdapter.KonuHolder>() {
    private lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    var isOnTextChanged = false
    var konuHash = hashMapOf<String, Int>()

    class KonuHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = KonuYanlisEditTextRowBinding.bind(itemView)

    }


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): KonuHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(
            com.karaketir.coachingapp.R.layout.konu_yanlis_edit_text_row, parent, false
        )

        val context = parent.context
        (context as Activity).window.decorView.findViewById<View>(R.id.content)

        return KonuHolder(view)
    }

    override fun onBindViewHolder(holder: KonuHolder, @SuppressLint("RecyclerView") position: Int) {

        with(holder) {

            if (konuListesi.isNotEmpty() && position >= 0 && position < konuListesi.size) {

                db = FirebaseFirestore.getInstance()
                auth = FirebaseAuth.getInstance()

                binding.denemeKonuAdi.text = konuListesi[position].subItemTitle


                binding.denemeKonuYanlisEditText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        isOnTextChanged = true
                    }

                    @SuppressLint("SetTextI18n")
                    override fun afterTextChanged(p0: Editable?) {
                        if (isOnTextChanged) {
                            isOnTextChanged = false


                            try {

                                konuHash[binding.denemeKonuAdi.text.toString()] =
                                    p0.toString().toInt()


                            } catch (e: Exception) {

                                konuHash[binding.denemeKonuAdi.text.toString()] = 0


                            }


                        }


                    }

                })


            }

        }


    }

    override fun getItemCount(): Int {

        return konuListesi.size
    }

}

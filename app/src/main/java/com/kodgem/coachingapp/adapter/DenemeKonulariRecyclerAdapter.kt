package com.kodgem.coachingapp.adapter

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kodgem.coachingapp.databinding.KonuYanlisEditTextRowBinding


open class DenemeKonulariRecyclerAdapter(
    private val konuListesi: ArrayList<String>,


    ) : RecyclerView.Adapter<DenemeKonulariRecyclerAdapter.KonuHolder>() {
    private lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    var toplamYanlis: Int = 0
    var isOnTextChanged = false
    var expAmtArray = ArrayList<String>()
    var textviewTotalExpense: TextView? = null
    var konuHash = hashMapOf<String, Int>()

    class KonuHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = KonuYanlisEditTextRowBinding.bind(itemView)

    }


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): KonuHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(
            com.kodgem.coachingapp.R.layout.konu_yanlis_edit_text_row, parent, false
        )

        val context = parent.context
        val rootView = (context as Activity).window.decorView.findViewById<View>(R.id.content)
        textviewTotalExpense =
            rootView.findViewById<View>(com.kodgem.coachingapp.R.id.totalText) as TextView


        return KonuHolder(view)
    }

    override fun onBindViewHolder(holder: KonuHolder, @SuppressLint("RecyclerView") position: Int) {
        with(holder) {
            db = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()

            binding.denemeKonuAdi.text = konuListesi[position]


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

                            toplamYanlis = 0
                            var i = 0
                            while (i <= position) {

                                if (i != position) {
                                    expAmtArray.add("0")
                                } else {
                                    expAmtArray.add("0")
                                    expAmtArray[position] = p0.toString()
                                    konuHash[binding.denemeKonuAdi.text.toString()] =
                                        p0.toString().toInt()
                                    break
                                }

                                i += 1
                            }

                            var j = 0
                            while (j <= expAmtArray.size - 1) {

                                val tempTotalExpenase = expAmtArray[i].toInt()
                                toplamYanlis += tempTotalExpenase


                                j += 1
                            }


                        } catch (e: Exception) {

                            toplamYanlis = 0

                            var i = 0

                            while (i <= position) {

                                if (i == position) {
                                    expAmtArray[position] = "0"
                                    konuHash[binding.denemeKonuAdi.text.toString()] = 0

                                }

                                i += 1

                            }
                            var j = 0
                            while (j <= expAmtArray.size - 1) {

                                val tempTotalExpenase = expAmtArray[i].toInt()
                                toplamYanlis += tempTotalExpenase


                                j += 1
                            }

                        }
                        var a = 0
                        for (i in expAmtArray) {
                            a += i.toInt()
                        }

                        textviewTotalExpense?.text = "Toplam Yanlış: $a"


                    }


                }

            })


        }


    }

    override fun getItemCount(): Int {

        return konuListesi.size
    }

}

package com.karaketir.coachingapp.models

class Item(
    var itemTitle: String, subItemList: ArrayList<SubItem>, var dersAdi: String, var tur: String
) {
    private var subItemList: ArrayList<SubItem>

    init {
        this.subItemList = subItemList
    }

    fun getSubItemList(): ArrayList<SubItem> {
        return subItemList
    }

}
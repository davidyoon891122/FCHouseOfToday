package com.example.fchouseoftoday

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.fchouseoftoday.data.ArticleModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class HomeFragment: Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore

        db.collection("articles").document("2xZK5PgFycebyjvusO9E")
            .get()
            .addOnSuccessListener { result ->
                val article = result.toObject<ArticleModel>()

                Log.e("HomeFragment", article.toString())
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

}
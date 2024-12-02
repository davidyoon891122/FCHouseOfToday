package com.example.fchouseoftoday.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fchouseoftoday.R
import com.example.fchouseoftoday.data.ArticleModel
import com.example.fchouseoftoday.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var articleAdapter: HomeArticleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        setupWriteButton(view)
        setupBookMarkButton(view)
        setupRecyclerView()
        fetchFirestoreData()

    }


    private fun setupWriteButton(view: View) {
        binding.writeButton.setOnClickListener {
            if (Firebase.auth.currentUser != null) {
                val action = HomeFragmentDirections.actionHomeFragmentToWriteArticleFragment()
                findNavController().navigate(action)
            } else {
                Snackbar.make(view, "로그인 후 사용해 주세요.", Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun setupBookMarkButton(view: View) {
        binding.bookmarkImageButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToBookMarkArticleFragment())
        }
    }

    private fun setupRecyclerView() {
        articleAdapter = HomeArticleAdapter(
            onItemClicked = {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToArticleFragment(
                        articleId = it.articleId.orEmpty()
                    )
                )
            },
            onBookmarkClicked = { articleId, isBookMark ->
                val uid = Firebase.auth.currentUser?.uid ?: return@HomeArticleAdapter
                Firebase.firestore.collection("bookmark").document(uid)
                    .update(
                        "articleIds",
                        if (isBookMark) {
                            FieldValue.arrayUnion(articleId)
                        } else {
                            FieldValue.arrayRemove(articleId)
                        }
                    )
                    .addOnFailureListener {
                        if (it is FirebaseFirestoreException && it.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                            if (isBookMark) {
                                Firebase.firestore.collection("bookmark").document(uid)
                                    .set(hashMapOf(
                                        "articleIds" to listOf(articleId)
                                    ))
                            }
                        }
                    }
            }
        )

        binding.homeRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = articleAdapter
        }
    }

    private fun fetchFirestoreData() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        Firebase.firestore.collection("bookmark")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val bookmarkList = it.get("articleIds") as? List<*>

                Firebase.firestore.collection("articles")
                    .get()
                    .addOnSuccessListener { result ->
                        val list = result.map { snapshot ->
                            snapshot.toObject<ArticleModel>()
                        }
                            .map { model ->
                                ArticleItem(
                                    articleId = model.articleId.orEmpty(),
                                    description = model.description.orEmpty(),
                                    imageUrl = model.imageUrl.orEmpty(),
                                    isBookMark = bookmarkList?.contains(model.articleId.orEmpty()) ?: false
                                )
                            }

                        articleAdapter.submitList(list)
                    }

            }
    }

}
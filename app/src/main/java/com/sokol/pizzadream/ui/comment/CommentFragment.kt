package com.sokol.pizzadream.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.CommentAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.R

class CommentFragment : Fragment() {
    private lateinit var commentsRecycler: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val commentViewModel = ViewModelProvider(this).get(CommentViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_comments, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            commentViewModel.comments.observe(viewLifecycleOwner, Observer {
                val listData = it
                val adapter = CommentAdapter(listData, requireContext())
                commentsRecycler.adapter = adapter
            })
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun initView(root: View) {
        commentsRecycler = root.findViewById(R.id.comments_recycler)
        commentsRecycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        commentsRecycler.layoutManager = layoutManager
        commentsRecycler.addItemDecoration(
            DividerItemDecoration(
                requireContext(), layoutManager.orientation
            )
        )
    }
}
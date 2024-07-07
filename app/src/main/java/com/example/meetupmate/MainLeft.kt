package com.example.meetupmate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainLeft : Fragment(), RecyclerViewHexagonAdapter.OnHexagonClickListener {

    lateinit var rvPostsMainLeft: RecyclerView
    lateinit var postsAdapter: RecyclerViewPostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(R.layout.fragment_main_left, container, false)

        rvPostsMainLeft = view.findViewById(R.id.rvPostsMainLeft)
        val llMainLeftWelcomeMessage: LinearLayout = view.findViewById(R.id.llMainLeftWelcomeMessage)

        DatabaseManager.getAllPosts { allPosts ->
                llMainLeftWelcomeMessage.visibility = GONE
                rvPostsMainLeft.visibility = VISIBLE

                rvPostsMainLeft.layoutManager = LinearLayoutManager(requireContext())
                postsAdapter = RecyclerViewPostsAdapter(allPosts, requireContext())
                rvPostsMainLeft.adapter = postsAdapter
        }

        DatabaseManager.getAllFriends { friends ->
            if (friends.isEmpty()) {
                rvPostsMainLeft.visibility = GONE
                llMainLeftWelcomeMessage.visibility = VISIBLE
            }
        }

        return view
    }

    override fun onHexagonClick(item: User) {
        Log.i("working", "working")
    }
}
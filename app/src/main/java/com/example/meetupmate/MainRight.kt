package com.example.meetupmate

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainRight : Fragment(), RecyclerViewHexagonAdapter.OnHexagonClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_main_right, container, false)

        val userHexagons = mutableListOf<User>()

        DatabaseManager.getAllFriends { friends ->
            for (friend in friends) {
                DatabaseManager.getUser(friend) { user ->
                    if (user != null) {
                        userHexagons.add(user)
                    }
                    if (userHexagons.size == friends.size) {
                        val rvPostsMainRight: RecyclerView = view.findViewById(R.id.rvPostsMainRight)
                        rvPostsMainRight.layoutManager = LinearLayoutManager(context)
                        rvPostsMainRight.adapter = RecyclerViewHexagonAdapter(userHexagons, this)
                    }
                }
            }
        }
        return view
    }

    override fun onHexagonClick(item: User) {
        val fragment = fragmentManager?.findFragmentById(R.id.fMainLeft) as MainLeft

        if (item.email == "") {
            DatabaseManager.getAllPosts { posts ->
                fragment.postsAdapter.update(posts)
            }
        }
        else {
            DatabaseManager.getPostsForUser(item.email) { posts ->
                fragment.postsAdapter.update(posts)
            }
        }
    }
}

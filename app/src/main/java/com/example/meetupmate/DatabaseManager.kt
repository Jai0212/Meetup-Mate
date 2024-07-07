package com.example.meetupmate

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object DatabaseManager {

    private val databaseReference = FirebaseDatabase.getInstance()
    lateinit var currUser: User

    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference: StorageReference = storage.getReference()

    // Adds/Updates User
    fun addNewUser(newUser: User, context: Context) {

        val image = R.drawable.profile_icon
        val imageUri = Uri.parse("android.resource://${context.packageName}/$image")

        val imageRef = storageReference.child("images_profiles/${imageUri.lastPathSegment}")
        val imageUrl = imageUri.toString()

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                Log.i("Image Upload", "Success")
            }
            .addOnFailureListener {
                Log.i("Image Upload", "Failure")
            }

        newUser.profileImage = imageUrl

        val newUserData: DatabaseReference = databaseReference.getReference(newUser.email.replace(".", ","))
        newUserData.setValue(newUser)
    }

    fun userExists(email: String, callback: (Boolean) -> Unit) {
        val emailRef: DatabaseReference = databaseReference.getReference(email.replace(".", ","))

        emailRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                val userExists = (user != null)
                callback(userExists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("ERROR", "Error: ${error.message}")
                callback(false)
            }
        })
    }

    fun getUser(email: String, callback: (User?) -> Unit) {
        val emailRef: DatabaseReference = databaseReference.getReference(email.replace(".", ","))

        emailRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("ERROR", "Error: ${error.message}")
                callback(null)
            }
        })
    }

    fun addPost(imageUri: Uri, addPostTitle: String, addPostDate: String, addPostTime: String, addPostDescription: String) {

        val imageRef = storageReference.child("images_posts/${imageUri.lastPathSegment}")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                Log.i("Image Upload", "Success")

                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    val newPost = Post(currUser.email, downloadUrl, addPostTitle, "$addPostDate, $addPostTime", addPostDescription)

                    val postsRef = databaseReference.getReference(currUser.email.replace(".", ",")).child("posts")
                    val newPostRef = postsRef.push()
                    newPostRef.setValue(newPost)

                    Log.d("Image Download", "Download URL: $downloadUrl")
                }
                    .addOnFailureListener { exception ->
                    Log.e("Image Download", "Failed to get download URL: $exception")
                }
            }
            .addOnFailureListener {
                Log.i("Image Upload", "Failure")
            }
    }

    fun initCurrUser(user: User) {
        currUser = user
    }

    fun getPostsForUser(email: String, callback: (List<Post>) -> Unit) {
        val userPostsRef = databaseReference.getReference(email.replace(".", ",")).child("posts")

        userPostsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()

                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let {
                        posts.add(it)
                    }
                }
                callback(posts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error retrieving posts: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun getAllUsers(callback: (List<User>) -> Unit) {
        val usersList = mutableListOf<User>()

        databaseReference.getReference().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        usersList.add(it)
                    }
                }
                callback(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error retrieving users: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun addFriend(friend: User) {
        val currUserEmail = currUser.email.replace(".", ",")
        val friendsRef = databaseReference.getReference("$currUserEmail/friends")

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listType = object : GenericTypeIndicator<ArrayList<String>>() {}
                val friendsList = snapshot.getValue(listType) ?: ArrayList()

                if (!friendsList.contains(friend.email)) {
                    friendsList.add(friend.email)
                    friendsRef.setValue(friendsList)
                        .addOnSuccessListener {
                            Log.d("DatabaseManager", "${friend.username} added as a friend.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("DatabaseManager", "Error adding friend: ${e.message}")
                        }
                } else {
                    Log.d("DatabaseManager", "${friend.username} is already a friend.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error adding friend: ${error.message}")
            }
        })
    }

    fun isFriend(friend: User, callback: (Boolean) -> Unit) {
        val currUserEmail = currUser.email.replace(".", ",")
        val friendsRef = databaseReference.getReference("$currUserEmail/friends")

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listType = object : GenericTypeIndicator<ArrayList<String>>() {}
                val friendsList = snapshot.getValue(listType)

                val isFriend = friendsList?.contains(friend.email) ?: false
                callback(isFriend)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error checking friends: ${error.message}")
                callback(false)
            }
        })
    }

    fun removeFriend(friend: User) {
        val currUserEmail = currUser.email.replace(".", ",")
        val friendsRef = databaseReference.getReference("$currUserEmail/friends")

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val listType = object : GenericTypeIndicator<ArrayList<String>>() {}
                val friendsList = snapshot.getValue(listType) ?: ArrayList()

                if (friendsList.contains(friend.email)) {
                    friendsList.remove(friend.email)
                    friendsRef.setValue(friendsList)
                        .addOnSuccessListener {
                            Log.d("DatabaseManager", "${friend.username} removed as a friend.")
                        }
                } else {
                    Log.d("DatabaseManager", "${friend.username} is not in friends list.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error removing friend: ${error.message}")
            }
        })
    }

    fun getAllFriends(user: User = currUser, callback: (List<String>) -> Unit) {
        val currUserEmail = user.email.replace(".", ",")
        val friendsRef = databaseReference.getReference("$currUserEmail/friends")

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listType = object : GenericTypeIndicator<ArrayList<String>>() {}
                val friendsList = snapshot.getValue(listType) ?: ArrayList()

                callback(friendsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error retrieving friends: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun getAllPosts(callback: (List<Post>) -> Unit) {
        val allPostsList = mutableListOf<Post>()

        getAllFriends { friends ->
            var remainingFriends = friends.size
            for (friend in friends) {
                getPostsForUser(friend) { posts ->
                    allPostsList.addAll(posts)
                    remainingFriends--
                    if (remainingFriends == 0) {
                        callback(allPostsList.shuffled())
                    }
                }
            }
        }
    }

    fun loadImageIntoImageView(imageUrl: String, imageView: ImageView) {
        Glide.with(imageView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image) // while loading
            .error(R.drawable.placeholder_image)
            .into(imageView)
    }

    fun joinPost(postToJoin: Post) {
        val postsRef = databaseReference.getReference(postToJoin.creatorEmail.replace(".", ",")).child("posts")

        postsRef.orderByChild("image").equalTo(postToJoin.image).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post == postToJoin) {

                        val joinedRef = postSnapshot.ref.child("joined")

                        joinedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(joinedSnapshot: DataSnapshot) {
                                val joinedList = joinedSnapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: ArrayList()

                                if (!joinedList.contains(currUser.email)) {
                                    joinedList.add(currUser.email)
                                    joinedRef.setValue(joinedList)
                                        .addOnSuccessListener {
                                            Log.d("DatabaseManager", "Successfully joined the post.")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("DatabaseManager", "Error joining the post: ${e.message}")
                                        }
                                } else {
                                    Log.d("DatabaseManager", "Already joined the post.")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("DatabaseManager", "Error retrieving joined list: ${error.message}")
                            }
                        })
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error retrieving post: ${error.message}")
            }
        })
    }

    fun leavePost(postToLeave: Post) {

        val postsRef = databaseReference.getReference(postToLeave.creatorEmail.replace(".", ",")).child("posts")

        postsRef.orderByChild("image").equalTo(postToLeave.image).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var postFound = false
                for (postSnapshot in snapshot.children) {
                    val foundPost = postSnapshot.getValue(Post::class.java)
                    if (foundPost == postToLeave) {
                        postFound = true

                        val joinedRef = postSnapshot.ref.child("joined")

                        joinedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(joinedSnapshot: DataSnapshot) {
                                val joinedList = joinedSnapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: ArrayList()

                                if (joinedList.contains(currUser.email)) {
                                    joinedList.remove(currUser.email)
                                    joinedRef.setValue(joinedList)
                                        .addOnSuccessListener {
                                            Log.d("DatabaseManager", "Successfully left the post.")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("DatabaseManager", "Error leaving the post: ${e.message}")
                                        }
                                } else {
                                    Log.d("DatabaseManager", "User is not joined in the post.")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("DatabaseManager", "Error retrieving joined list: ${error.message}")
                            }
                        })
                        break
                    }
                }
                if (!postFound) {
                    Log.d("DatabaseManager", "Post not found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error retrieving post: ${error.message}")
            }
        })
    }

    fun hasJoinedPost(post: Post, callback: (Boolean) -> Unit) {

        val postsRef = databaseReference.getReference(post.creatorEmail.replace(".", ",")).child("posts")

        postsRef.orderByChild("image").equalTo(post.image).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var postFound = false

                for (postSnapshot in snapshot.children) {
                    val foundPost = postSnapshot.getValue(Post::class.java)
                    if (foundPost == post) {
                        postFound = true

                        val joinedRef = postSnapshot.ref.child("joined")

                        joinedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(joinedSnapshot: DataSnapshot) {
                                val joinedList = joinedSnapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: ArrayList()
                                val isJoined = joinedList.contains(currUser.email)
                                callback(isJoined)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("DatabaseManager", "Error retrieving joined list: ${error.message}")
                                callback(false)
                            }
                        })
                        break
                    }
                }
                if (!postFound) {
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error retrieving post: ${error.message}")
                callback(false)
            }
        })
    }

    fun allJoinedPost(post: Post, callback: (List<String>) -> Unit) {
        val postsRef = databaseReference.getReference(post.creatorEmail.replace(".", ",")).child("posts")

        postsRef.orderByChild("image").equalTo(post.image).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val fetchedPost = postSnapshot.getValue(Post::class.java)

                    if (fetchedPost == post) {
                        val joinedRef = postSnapshot.ref.child("joined")

                        joinedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(joinedSnapshot: DataSnapshot) {
                                val joinedList = joinedSnapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: ArrayList()
                                joinedList.add(post.creatorEmail)
                                callback(joinedList)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("DatabaseManager", "Error retrieving joined list: ${error.message}")
                                callback(emptyList())
                            }
                        })
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error retrieving post: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun deletePost(postToDelete: Post) {
        val postsRef = databaseReference.getReference(postToDelete.creatorEmail.replace(".", ",")).child("posts")

        postsRef.orderByChild("image").equalTo(postToDelete.image).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var postFound = false
                for (postSnapshot in snapshot.children) {
                    val foundPost = postSnapshot.getValue(Post::class.java)
                    if (foundPost == postToDelete) {
                        postFound = true

                        postSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Log.d("DatabaseManager", "Post deleted successfully.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("DatabaseManager", "Error deleting post: ${e.message}")
                            }
                        break
                    }
                }
                if (!postFound) {
                    Log.d("DatabaseManager", "Post not found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseManager", "Error retrieving post: ${error.message}")
            }
        })
    }

    fun updateProfileImage(imageProfileUri: Uri) {
        val imageRef = storageReference.child("images_profiles/${imageProfileUri.lastPathSegment}")

        imageRef.putFile(imageProfileUri)
            .addOnSuccessListener {
                Log.i("Image Upload", "Success")

                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    val profileImageRef: DatabaseReference = databaseReference.getReference(currUser.email.replace(".", ",")).child("profileImage")
                    profileImageRef.setValue(downloadUrl)

                    currUser.profileImage = downloadUrl

                    Log.d("Image Download", "Download URL: $downloadUrl")
                }
                    .addOnFailureListener { exception ->
                        Log.e("Image Download", "Failed to get download URL: $exception")
                    }
            }
            .addOnFailureListener {
                Log.i("Image Upload", "Failure")
            }
    }

    fun updateProfileImageWithURL(imageProfileURL: String) {
        val profileImageRef: DatabaseReference = databaseReference.getReference(currUser.email.replace(".", ",")).child("profileImage")
        profileImageRef.setValue(imageProfileURL)

        currUser.profileImage = imageProfileURL
    }

    fun getProfileImage(email: String = currUser.email, callback: (String?) -> Unit) {
        val profileImageRef: DatabaseReference = databaseReference.getReference()
            .child(email.replace(".", ","))
            .child("profileImage")

        profileImageRef.get().addOnSuccessListener { dataSnapshot ->
            val profileImageUrl = dataSnapshot.getValue(String::class.java)
            callback(profileImageUrl)
        }.addOnFailureListener {
            Log.i("DatabaseManager", "Error getting profile image")
            callback(null)
        }
    }

    fun usernameExists(username: String, callback: (Boolean) -> Unit) {
        getAllUsers { users ->
            for (user in users) {
                if (user.username == username) {
                    callback(true)
                }
            }
        }
        callback(false)
    }
}

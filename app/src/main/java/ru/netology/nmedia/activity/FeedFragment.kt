package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener{
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onUnLike(post: Post) {
                viewModel.unLikeById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })
        binding.list.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading || state.refreshing
            binding.swipeRefresh.isVisible = !state.refreshing
            binding.errorGroup.isVisible = state.error
            if(state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadPosts()
                    }
                    .show()
            }
        }

        viewModel.data.observe(viewLifecycleOwner) {
            binding.emptyText.isVisible = it.empty //видимость, если есть флаг empty
            val newPost = it.posts.size > adapter.itemCount //проверка на действие добавления поста, а не другое действие
            Log.d("posts size: ", it.posts.size.toString())
            Log.d("adapter itemCount: ", adapter.itemCount.toString())
            adapter.submitList(it.posts) {
                if(newPost) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            viewModel.refresh()
        }

        viewModel.postsLoadError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), viewModel.postsLoadError.value, Toast.LENGTH_LONG).show()
        }

        viewModel.savePostError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), viewModel.savePostError.value, Toast.LENGTH_LONG).show()
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            Log.d("FeedFragment", "newer count: $it")
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            Log.d("FeedFragment", "newer count: $it")
            val text = "${getString(R.string.new_notes)} ($it)"
            binding.toNewPostsButton.text = text
            binding.toNewPostsButton.isVisible = it != 0
        }

        binding.toNewPostsButton.isVisible = false

        binding.toNewPostsButton.setOnClickListener {
            binding.toNewPostsButton.isVisible = false
            viewModel.changeHiddenStatus()
        }


        return binding.root
    }
}

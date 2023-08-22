package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import ru.netology.nmedia.Auth.AppAuth
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

class AppActivity : AppCompatActivity(R.layout.activity_app) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val postBinding = CardPostBinding.inflate(layoutInflater)

        requestNotificationsPermission()

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        checkGoogleApiAvailability()

        val authViewModel by viewModels<AuthViewModel>()

        var currentMenuProvider: MenuProvider? = null

        authViewModel.data.observe(this) {
            //в зависимости от появления нового токена,
            //будет исчезать или появляться новый menuProvider

            currentMenuProvider.let { ::removeMenuProvider }

            // подписка, чтобы какждый раз перестраивать меню
            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_auth, menu)

                    val authenticated = authViewModel.isAuthenticated

                    menu.setGroupVisible(R.id.authorized, authenticated)
                    menu.setGroupVisible(R.id.unathorized, !authenticated)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.signIn -> {
                            findNavController(R.id.nav_host_fragment)
                                .navigate(R.id.action_feedFragment_to_authFragment)
                            AppAuth.getInstance().setAuth(5, "x-token")
                            true
                        }

                        R.id.signUp -> {
                            //TODO: Make it in homework
                            AppAuth.getInstance().setAuth(5, "x-token")
                            true
                        }

                        R.id.logout -> {
                            //TODO: Make it in homework
                            AppAuth.getInstance().clearAuth()
                            true
                        }

                        else -> false
                    }
            }.also {
                   currentMenuProvider = it
            }, this)
        }
    }


    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS

        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(
                this@AppActivity,
                R.string.google_play_unavailable,
                Toast.LENGTH_LONG
            )
                .show()
        }

//        FirebaseMessaging.getInstance().token.addOnSuccessListener {
//            println(it)
//        }

    }
}
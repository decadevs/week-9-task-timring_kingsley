package com.example.pokemonapp

import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ColorStateListInflaterCompat.inflate
import androidx.core.content.res.ComplexColorCompat.inflate
import androidx.core.graphics.drawable.DrawableCompat.inflate
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_base.view.*

abstract class BaseActivity: AppCompatActivity() {
   lateinit var progressBar: ProgressBar

    override fun setContentView(layoutResID: Int) {
        val constraintLayout = layoutInflater.inflate(R.layout.activity_base, null) as ConstraintLayout
        val frameLayout = constraintLayout.activity_content
        progressBar = constraintLayout.progress_bar

        layoutInflater.inflate(layoutResID, frameLayout, true)
        super.setContentView(layoutResID)
    }

    fun showProgressBar(visibility: Boolean) {
        progressBar.visibility = if( progressBar.isVisible ) View.VISIBLE else View.INVISIBLE
    }
}
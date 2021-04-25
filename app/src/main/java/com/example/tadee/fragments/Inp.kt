package com.example.tadee.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.tadee.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_inp.*

class Inp : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.example.tadee.R.layout.fragment_inp, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = listOf("Short", "Medium", "Long")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        modeltext.setAdapter(adapter)

        toggleButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId){
                R.id.sym -> {
                    d2Layout.visibility = View.GONE
                    d3Layout.visibility = View.GONE
                }
                R.id.unsym -> {
                    d2Layout.visibility = View.VISIBLE
                    d3Layout.visibility = View.VISIBLE
                }
            }
        }



    }

}
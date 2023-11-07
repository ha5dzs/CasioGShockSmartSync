/*
 * Created by Ivo Zivkov (izivkov@gmail.com) on 2022-03-30, 12:06 a.m.
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 2022-03-22, 1:57 p.m.
 */

package org.avmedia.gShockPhoneSync.ui.alarms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import org.avmedia.gShockPhoneSync.databinding.FragmentAlarmsBinding

class AlarmsFragment : Fragment() {

    private var _binding: FragmentAlarmsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        alarmsFragmentScope = lifecycleScope // call this first

        _binding = FragmentAlarmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        var alarmsFragmentScope: LifecycleCoroutineScope? = null
    }
}
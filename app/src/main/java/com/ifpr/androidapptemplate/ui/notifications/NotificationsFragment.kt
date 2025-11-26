package com.ifpr.androidapptemplate.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R

class NotificationsFragment : Fragment() {


    private val viewModel: NotificationsViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter

    private lateinit var emptyStateContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_notifications)
        emptyStateContainer = view.findViewById(R.id.empty_state_container)

        configurarRecyclerView()
        observarViewModel()
    }

    private fun configurarRecyclerView() {
        adapter = NotificationAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun observarViewModel() {
        viewModel.notificationList.observe(viewLifecycleOwner) { notifications ->
            adapter.listaNotificacoes = notifications

            if (notifications.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyStateContainer.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyStateContainer.visibility = View.GONE
            }
        }
    }
}
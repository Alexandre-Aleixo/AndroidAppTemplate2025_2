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

    // Inicializa o ViewModel usando a delegação viewModels()
    private val viewModel: NotificationsViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    // Assumimos que existe um container para exibir quando a lista estiver vazia
    private lateinit var emptyStateContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Usa o layout que você já possui para esta tela
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mapeia os componentes do layout (Verifique se esses IDs estão no seu fragment_notifications.xml!)
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
        // Observa o LiveData da lista de notificações do ViewModel
        viewModel.notificationList.observe(viewLifecycleOwner) { notifications ->
            adapter.listaNotificacoes = notifications

            // Lógica para mostrar/esconder o empty state
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
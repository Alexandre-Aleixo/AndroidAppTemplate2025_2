package com.ifpr.androidapptemplate.ui.tarefas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Tarefa
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaTarefaFragment : Fragment(), TarefaAcoesListener {

    private val viewModel: TarefaViewModel by viewModels()

    private lateinit var tarefaAdapter: TarefaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdicionarTarefa: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_tarefas)
        fabAdicionarTarefa = view.findViewById(R.id.fab_adicionar_tarefa)

        configurarRecyclerView()
        configurarListeners()
        observarViewModel()
    }

    private fun configurarRecyclerView() {
        tarefaAdapter = TarefaAdapter(this)
        recyclerView.adapter = tarefaAdapter
    }

    private fun configurarListeners() {
        fabAdicionarTarefa.setOnClickListener {
            mostrarDialogoAdicionarTarefa()
        }
    }

    private fun observarViewModel() {
        viewModel.listaTarefas.observe(viewLifecycleOwner) { tarefas ->
            tarefaAdapter.atualizarLista(tarefas)
        }
    }

    override fun onStatusAlterado(tarefa: Tarefa, estaConcluida: Boolean) {
        viewModel.atualizarStatusTarefa(tarefa, estaConcluida)
    }

    private fun mostrarDialogoAdicionarTarefa() {
        val editText = EditText(context)

        AlertDialog.Builder(requireContext())
            .setTitle("Nova Tarefa")
            .setView(editText)
            .setPositiveButton("Adicionar") { dialog, _ ->
                val descricao = editText.text.toString().trim()
                if (descricao.isNotEmpty()) {
                    viewModel.adicionarTarefa(descricao)
                } else {
                    Toast.makeText(context, "A descrição não pode ser vazia.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}
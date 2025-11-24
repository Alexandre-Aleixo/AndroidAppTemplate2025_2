package com.ifpr.androidapptemplate.ui.tarefas

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Tarefa
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class ListaTarefaFragment : Fragment(), TarefaAcoesListener {

    private val viewModel: TarefaViewModel by viewModels()

    private lateinit var tarefaAdapter: TarefaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdicionarTarefa: FloatingActionButton
    private lateinit var emptyStateContainer: LinearLayout

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
        emptyStateContainer = view.findViewById(R.id.empty_state_container)

        configurarRecyclerView()
        configurarListeners()
        observarViewModel()
        configurarSwipeParaDeletar()
    }

    private fun configurarRecyclerView() {
        tarefaAdapter = TarefaAdapter(this, ::onTarefaClick)
        recyclerView.adapter = tarefaAdapter
    }

    private fun onTarefaClick(tarefa: Tarefa) {
        mostrarDialogoEdicaoTarefa(tarefa)
    }

    private fun configurarListeners() {
        fabAdicionarTarefa.setOnClickListener {
            mostrarDialogoAdicionarTarefa()
        }
    }

    private fun observarViewModel() {
        viewModel.listaTarefas.observe(viewLifecycleOwner) { tarefas ->
            tarefaAdapter.atualizarLista(tarefas)

            if (tarefas.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyStateContainer.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyStateContainer.visibility = View.GONE
            }
        }
    }

    override fun onStatusAlterado(tarefa: Tarefa, estaConcluida: Boolean) {
        viewModel.atualizarStatusTarefa(tarefa, estaConcluida)
    }

    private fun configurarSwipeParaDeletar() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean { return false }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val tarefaParaDeletar = tarefaAdapter.listaTarefas[position]

                viewModel.deletarTarefa(tarefaParaDeletar)

                Snackbar.make(
                    requireView(),
                    "Tarefa '${tarefaParaDeletar.descricao}' removida.",
                    Snackbar.LENGTH_LONG
                ).setAction("DESFAZER") {
                    viewModel.adicionarTarefa(tarefaParaDeletar.descricao)
                }.show()
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.red_delete))
                val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_black_24dp)

                val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = itemView.bottom - iconMargin

                if (dX > 0) {
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + deleteIcon.intrinsicWidth
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                } else if (dX < 0) {
                    val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                } else {
                    background.setBounds(0, 0, 0, 0)
                }

                background.draw(c)
                if (dX != 0f) deleteIcon.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun mostrarDialogoEdicaoTarefa(tarefa: Tarefa) {
        val editText = EditText(context)
        editText.setText(tarefa.descricao)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Tarefa")
            .setView(editText)
            .setPositiveButton("Salvar") { dialog, _ ->
                val novaDescricao = editText.text.toString().trim()
                if (novaDescricao.isNotEmpty() && novaDescricao != tarefa.descricao) {
                    val tarefaAtualizada = tarefa.copy(descricao = novaDescricao)

                    viewModel.atualizarDescricaoTarefa(tarefaAtualizada)

                    Toast.makeText(context, "Tarefa atualizada.", Toast.LENGTH_SHORT).show()
                } else if (novaDescricao.isEmpty()) {
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
package com.ifpr.androidapptemplate.ui.tarefas

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.PopupMenu
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Tarefa
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import android.widget.LinearLayout
import android.widget.TextView // NOVA IMPORTAÇÃO NECESSÁRIA

// ESTE CÓDIGO ESTÁ CORRETO. ELE SÓ FUNCIONARÁ QUANDO A INTERFACE FOR CORRIGIDA.

class ListaTarefaFragment : Fragment(), TarefaAcoesListener {

    private val viewModel: TarefaViewModel by viewModels()

    private lateinit var tarefaAdapter: TarefaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdicionarTarefa: FloatingActionButton
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var searchView: SearchView
    // NOVO: Variável para a mensagem de "Nenhum Resultado Encontrado"
    private lateinit var noResultsMessage: TextView

    private var btnOrdenar: ImageButton? = null

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
        btnOrdenar = view.findViewById(R.id.btn_ordenar)
        searchView = view.findViewById(R.id.search_view)
        // NOVO: Inicializa a TextView de resultados vazios
        noResultsMessage = view.findViewById(R.id.text_no_results)

        configurarRecyclerView()
        configurarListeners()
        // NOTA: 'observarViewModel' será ajustado abaixo.
        observarViewModel()
        configurarSwipeParaDeletar()

        configurarSearchView()

        btnOrdenar?.setOnClickListener {
            mostrarMenuOrdenacao(it)
        }
    }

    private fun configurarRecyclerView() {
        tarefaAdapter = TarefaAdapter(this, ::onTarefaClick)
        recyclerView.adapter = tarefaAdapter
    }

    private fun configurarSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        searchView.setOnCloseListener {
            viewModel.setSearchQuery("")
            true
        }
    }


    private fun onTarefaClick(tarefa: Tarefa) {
        mostrarDialogoEdicaoTarefa(tarefa)
    }

    private fun configurarListeners() {
        fabAdicionarTarefa.setOnClickListener {
            mostrarDialogoAdicionarTarefa()
        }
    }

    // ####################################################################
    // Lógica de Visibilidade ATUALIZADA (3 Estados)
    // ####################################################################
    private fun observarViewModel() {
        viewModel.listaTarefas.observe(viewLifecycleOwner) { tarefas ->
            tarefaAdapter.atualizarLista(tarefas)

            val currentQuery = viewModel.searchQuery.value.orEmpty().trim()
            val isSearching = currentQuery.isNotEmpty()

            if (tarefas.isEmpty()) {
                recyclerView.visibility = View.GONE

                if (isSearching) {
                    // Caso 1: Busca ativa, mas não encontrou resultados
                    emptyStateContainer.visibility = View.GONE // Esconde o padrão
                    noResultsMessage.visibility = View.VISIBLE // Mostra a mensagem de busca vazia
                    // Use a string parametrizada (R.string.no_results_found)
                    noResultsMessage.text = getString(R.string.no_results_found, currentQuery)
                } else {
                    // Caso 2: Nenhuma busca ativa e lista de tarefas vazia (primeiro uso)
                    emptyStateContainer.visibility = View.VISIBLE // Mostra o Empty State padrão
                    noResultsMessage.visibility = View.GONE
                }
            } else {
                // Caso 3: Lista tem resultados
                recyclerView.visibility = View.VISIBLE
                emptyStateContainer.visibility = View.GONE
                noResultsMessage.visibility = View.GONE
            }
        }
    }
    // ####################################################################

    // ... (Restante do código inalterado) ...

    override fun onStatusAlterado(tarefa: Tarefa, estaConcluida: Boolean) {
        viewModel.atualizarStatusTarefa(tarefa, estaConcluida)
    }

    override fun onDeletarTarefa(tarefa: Tarefa) {
        viewModel.deletarTarefa(tarefa)
    }

    private fun mostrarMenuOrdenacao(view: View) {
        val popup = PopupMenu(requireContext(), view)

        popup.menu.add(0, OpcaoOrdenacao.STATUS.ordinal, 0, "Status (Pendentes primeiro)")
        popup.menu.add(0, OpcaoOrdenacao.ALFABETICA.ordinal, 1, "Descrição (A-Z)")
        popup.menu.add(0, OpcaoOrdenacao.MAIS_RECENTE.ordinal, 2, "Data (Mais Recentes)")

        popup.setOnMenuItemClickListener { menuItem ->
            val opcaoSelecionada = when (menuItem.itemId) {
                OpcaoOrdenacao.STATUS.ordinal -> OpcaoOrdenacao.STATUS
                OpcaoOrdenacao.ALFABETICA.ordinal -> OpcaoOrdenacao.ALFABETICA
                OpcaoOrdenacao.MAIS_RECENTE.ordinal -> OpcaoOrdenacao.MAIS_RECENTE
                else -> return@setOnMenuItemClickListener false
            }

            viewModel.setOrdenacao(opcaoSelecionada)
            Toast.makeText(context, "Ordenado por: ${menuItem.title}", Toast.LENGTH_SHORT).show()
            true
        }

        popup.show()
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

                viewModel.deletarTarefa(tarefaParaDeletar) // CORRETO

                Snackbar.make(
                    requireView(),
                    "Tarefa '${tarefaParaDeletar.descricao}' removida.",
                    Snackbar.LENGTH_LONG
                ).setAction("DESFAZER") {
                    viewModel.adicionarTarefa( // CORRETO
                        tarefaParaDeletar.descricao,
                        tarefaParaDeletar.iconeResId,
                        tarefaParaDeletar.prazo
                    )
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
        val dialog = AdicionarTarefaDialogFragment.newInstance(tarefa)
        dialog.show(childFragmentManager, AdicionarTarefaDialogFragment.TAG)
    }

    private fun mostrarDialogoAdicionarTarefa() {
        val dialog = AdicionarTarefaDialogFragment.newInstance()
        dialog.show(childFragmentManager, AdicionarTarefaDialogFragment.TAG)
    }
}
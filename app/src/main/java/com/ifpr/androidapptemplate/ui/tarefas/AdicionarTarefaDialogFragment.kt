package com.ifpr.androidapptemplate.ui.tarefas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Tarefa
import com.ifpr.androidapptemplate.databinding.DialogAdicionarTarefaBinding
import com.ifpr.androidapptemplate.utils.IconeHelper

class AdicionarTarefaDialogFragment : DialogFragment() {

    private val viewModel: TarefaViewModel by activityViewModels()
    private var tarefaParaEdicao: Tarefa? = null

    private var _binding: DialogAdicionarTarefaBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tarefaParaEdicao = arguments?.getParcelable(ARG_TAREFA)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        _binding = DialogAdicionarTarefaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarSpinner()

        if (tarefaParaEdicao != null) {
            binding.editTextNovaTarefa.setText(tarefaParaEdicao!!.descricao)
            binding.buttonAdicionar.text = getString(R.string.salvar)

            // Define o Spinner na categoria atual
            val iconeAtualIndex = IconeHelper.iconesDisponiveis.indexOfFirst { it.drawableResId == tarefaParaEdicao!!.iconeResId }
            if (iconeAtualIndex != -1) {
                binding.spinnerIconeTarefa.setSelection(iconeAtualIndex)
            }
        } else {
            binding.buttonAdicionar.text = getString(R.string.adicionar)
        }

        binding.buttonAdicionar.setOnClickListener {
            salvarTarefa()
        }
    }

    private fun configurarSpinner() {
        val nomesIcones = IconeHelper.iconesDisponiveis.map { it.nome }

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            nomesIcones
        )
        binding.spinnerIconeTarefa.adapter = spinnerAdapter
    }

    private fun salvarTarefa() {
        val descricao = binding.editTextNovaTarefa.text.toString().trim()
        val posicaoSelecionada = binding.spinnerIconeTarefa.selectedItemPosition
        val iconeSelecionadoResId = IconeHelper.iconesDisponiveis[posicaoSelecionada].drawableResId

        if (descricao.isNotEmpty()) {
            if (tarefaParaEdicao != null) {
                val tarefaAtualizada = tarefaParaEdicao!!.copy(
                    descricao = descricao,
                    iconeResId = iconeSelecionadoResId
                )
                viewModel.atualizarDescricaoTarefa(tarefaAtualizada)
                Toast.makeText(context, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.adicionarTarefa(descricao, iconeSelecionadoResId)
                Toast.makeText(context, "Tarefa adicionada!", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        } else {
            Toast.makeText(context, "A descrição não pode ser vazia.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AdicionarTarefaDialog"
        private const val ARG_TAREFA = "tarefa_a_editar"

        fun newInstance(): AdicionarTarefaDialogFragment {
            return AdicionarTarefaDialogFragment()
        }

        fun newInstance(tarefa: Tarefa): AdicionarTarefaDialogFragment {
            val fragment = AdicionarTarefaDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_TAREFA, tarefa)
            fragment.arguments = args
            return fragment
        }
    }
}
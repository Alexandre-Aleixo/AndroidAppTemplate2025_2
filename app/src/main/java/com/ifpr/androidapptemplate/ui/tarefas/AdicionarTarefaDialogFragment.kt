package com.ifpr.androidapptemplate.ui.tarefas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.ifpr.androidapptemplate.utils.formatarPrazoParaExibicao
import com.ifpr.androidapptemplate.utils.IconeHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AdicionarTarefaDialogFragment : DialogFragment() {

    private val viewModel: TarefaViewModel by activityViewModels()
    private var tarefaParaEdicao: Tarefa? = null

    private var prazoSelecionado: Long? = null

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
        configurarListenersPrazo()

        if (tarefaParaEdicao != null) {
            preencherDadosEdicao()
            binding.buttonAdicionar.text = getString(R.string.salvar)
        } else {
            binding.buttonAdicionar.text = getString(R.string.adicionar)
        }

        binding.buttonAdicionar.setOnClickListener {
            salvarTarefa()
        }
    }

    private fun preencherDadosEdicao() {
        tarefaParaEdicao?.let { tarefa ->
            binding.editTextNovaTarefa.setText(tarefa.descricao)

            // Usa o prazo existente para inicializar
            prazoSelecionado = tarefa.prazo

            atualizarExibicaoPrazo(tarefa.prazo)

            val iconeAtualIndex = IconeHelper.iconesDisponiveis.indexOfFirst { it.drawableResId == tarefa.iconeResId }
            if (iconeAtualIndex != -1) {
                binding.spinnerIconeTarefa.setSelection(iconeAtualIndex)
            }
        }
    }

    private fun configurarListenersPrazo() {
        binding.layoutPrazoSeletor.setOnClickListener {
            mostrarPickerDialogs()
        }

        binding.buttonLimparPrazo.setOnClickListener {
            prazoSelecionado = null
            atualizarExibicaoPrazo(null)
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

    private fun mostrarPickerDialogs() {
        val calendario = Calendar.getInstance()

        prazoSelecionado?.let {
            calendario.timeInMillis = it
        }

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        // Configura a data e hora selecionadas
                        calendario.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        calendario.set(Calendar.MILLISECOND, 0) // Zera milissegundos para comparação mais limpa

                        val novoPrazo = calendario.timeInMillis
                        val agora = System.currentTimeMillis()

                        // ⚠️ Validação Adicional: Verifica se a data/hora selecionada está no passado
                        if (novoPrazo <= agora) {
                            Toast.makeText(requireContext(), "O prazo deve ser uma data e hora futura.", Toast.LENGTH_LONG).show()
                            // Não atualiza prazoSelecionado
                        } else {
                            prazoSelecionado = novoPrazo
                            atualizarExibicaoPrazo(prazoSelecionado)
                        }
                    },
                    calendario.get(Calendar.HOUR_OF_DAY),
                    calendario.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
        // Impede que o usuário selecione datas no DatePickerDialog que já passaram
        datePicker.datePicker.minDate = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)
        datePicker.show()
    }

    private fun atualizarExibicaoPrazo(prazo: Long?) {
        if (prazo == null) {
            binding.textViewPrazo.text = "Nenhum prazo definido"
            binding.textViewPrazo.setTextColor(resources.getColor(android.R.color.darker_gray))
        } else {
            // Usa o formatador de extensão (assumindo que ele está no tipo Long)
            binding.textViewPrazo.text = prazo.formatarPrazoParaExibicao()
            binding.textViewPrazo.setTextColor(resources.getColor(R.color.colorPrimary))
        }
    }

    private fun salvarTarefa() {
        val descricao = binding.editTextNovaTarefa.text.toString().trim()
        val posicaoSelecionada = binding.spinnerIconeTarefa.selectedItemPosition
        val iconeSelecionadoResId = IconeHelper.iconesDisponiveis[posicaoSelecionada].drawableResId

        // 1. Validação da Descrição
        if (descricao.isEmpty()) {
            Toast.makeText(context, "A descrição não pode ser vazia.", Toast.LENGTH_SHORT).show()
            return // Impede salvar
        }

        // 2. Validação FINAL do Prazo
        val agora = System.currentTimeMillis()
        if (prazoSelecionado != null && prazoSelecionado!! <= agora) {
            Toast.makeText(context, "O prazo FINAL deve ser uma data e hora futura. Ajuste ou limpe o campo.", Toast.LENGTH_LONG).show()
            return // Impede salvar
        }

        // 3. Salvar/Atualizar
        if (tarefaParaEdicao != null) {
            val tarefaAtualizada = tarefaParaEdicao!!.copy(
                descricao = descricao,
                iconeResId = iconeSelecionadoResId,
                prazo = prazoSelecionado
            )
            viewModel.atualizarDescricaoTarefa(tarefaAtualizada)
            Toast.makeText(context, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.adicionarTarefa(descricao, iconeSelecionadoResId, prazoSelecionado)
            Toast.makeText(context, "Tarefa adicionada!", Toast.LENGTH_SHORT).show()
        }
        dismiss()
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
package com.aditya.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

class TransactionAdapter(
    private var transactions: MutableList<Transaction>,  // Make it mutable for easier updates
    private val transactionDao: TransactionDao,
    private val onTotalsChanged: (Double, Double) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val statusTextView: TextView = view.findViewById(R.id.statusTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val amountTextView: TextView = view.findViewById(R.id.amountTextView)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_row, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.nameTextView.text = transaction.name
        holder.statusTextView.text = transaction.transactionType.capitalize(Locale.getDefault())
        holder.dateTextView.text = transaction.date

        // Convert the double amount to an integer
        val displayAmount = transaction.amount.toInt()

        if (transaction.transactionType == "borrowed") {
            holder.amountTextView.text = "-₹$displayAmount"
            holder.amountTextView.setTextColor(holder.itemView.context.getColor(R.color.red))
            holder.statusTextView.setTextColor(holder.itemView.context.getColor(R.color.red))
        } else if (transaction.transactionType == "lent") {
            holder.amountTextView.text = "₹$displayAmount"
            holder.amountTextView.setTextColor(holder.itemView.context.getColor(R.color.green)) // #4caf50 color
            holder.statusTextView.setTextColor(holder.itemView.context.getColor(R.color.green))
        }

        holder.deleteButton.setOnClickListener {
            GlobalScope.launch {
                transactionDao.delete(transaction)

                // Ensure changes are made on the main thread
                holder.itemView.post {
                    transactions.removeAt(position)
                    notifyItemRemoved(position)
                    calculateTotals()
                }
            }
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
        calculateTotals()
    }

    private fun calculateTotals() {
        val totalBorrowed = transactions.filter { it.transactionType == "borrowed" }.sumOf { it.amount }
        val totalLent = transactions.filter { it.transactionType == "lent" }.sumOf { it.amount }
        onTotalsChanged(totalBorrowed, totalLent)
    }
}